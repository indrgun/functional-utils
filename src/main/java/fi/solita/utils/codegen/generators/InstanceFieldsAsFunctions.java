package fi.solita.utils.codegen.generators;

import static fi.solita.utils.codegen.Helpers.element2Fields;
import static fi.solita.utils.codegen.Helpers.qualifiedName;
import static fi.solita.utils.codegen.Helpers.elementClass;
import static fi.solita.utils.codegen.Helpers.elementGenericQualifiedName;
import static fi.solita.utils.codegen.Helpers.hasNonQmarkGenerics;
import static fi.solita.utils.codegen.Helpers.hasTypeParameters;
import static fi.solita.utils.codegen.Helpers.isPrivate;
import static fi.solita.utils.codegen.Helpers.resolveBoxedGenericType;
import static fi.solita.utils.codegen.Helpers.resolveVisibility;
import static fi.solita.utils.codegen.Helpers.staticElements;
import static fi.solita.utils.codegen.Helpers.typeParameter2String;
import static fi.solita.utils.codegen.generators.Content.EmptyLine;
import static fi.solita.utils.codegen.generators.Content.None;
import static fi.solita.utils.codegen.generators.Content.catchBlock;
import static fi.solita.utils.codegen.generators.Content.memberAccessor;
import static fi.solita.utils.codegen.generators.Content.memberInitializer;
import static fi.solita.utils.codegen.generators.Content.memberNameAccessor;
import static fi.solita.utils.functional.Collections.newList;
import static fi.solita.utils.functional.Functional.concat;
import static fi.solita.utils.functional.Functional.filter;
import static fi.solita.utils.functional.Functional.flatMap;
import static fi.solita.utils.functional.Functional.isEmpty;
import static fi.solita.utils.functional.Functional.map;
import static fi.solita.utils.functional.Functional.mkString;
import static fi.solita.utils.functional.Functional.repeat;
import static fi.solita.utils.functional.Option.Some;
import static fi.solita.utils.functional.Predicates.not;
import static fi.solita.utils.functional.Transformers.prepend;

import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

import fi.solita.utils.functional.Apply;
import fi.solita.utils.functional.Collections;
import fi.solita.utils.functional.Function1;
import fi.solita.utils.functional.Function2;

public class InstanceFieldsAsFunctions extends Function2<InstanceFieldsAsFunctions.Options, TypeElement, Iterable<String>> {
    
    public static final Function1<Options, Function1<TypeElement, Iterable<String>>> instance = new InstanceFieldsAsFunctions().curried();
    
    @SuppressWarnings("rawtypes")
    public static interface Options {
        Class<? extends Apply> getClassForInstanceFields();
        Class<? extends Apply> getPredicateClassForInstanceFields();
        List<String> getAdditionalBodyLinesForInstanceFields();
        boolean generateMemberAccessorForFields();
        boolean generateMemberInitializerForFields();
        boolean generateMemberNameAccessorForFields();
        boolean makeFieldsPublic();
    }
    
    @Override
    public Iterable<String> apply(Options options, TypeElement source) {
        return flatMap(filter(element2Fields.apply(source), not(staticElements)), variableElementGen.curried().apply(options));
    }
    
    public static Function2<Options, VariableElement, Iterable<String>> variableElementGen = new Function2<InstanceFieldsAsFunctions.Options, VariableElement, Iterable<String>>() {
        @Override
        public Iterable<String> apply(Options options, VariableElement field) {
            TypeElement enclosingElement = (TypeElement) field.getEnclosingElement();
            
            List<String> relevantTypeParams = newList(map(enclosingElement.getTypeParameters(), typeParameter2String));
            String relevantTypeParamsString = isEmpty(relevantTypeParams) ? "" : "<" + mkString(", ", relevantTypeParams) + ">";

            String returnType = resolveBoxedGenericType(field.asType());
            
            boolean isPrivate = isPrivate(field);
            boolean needsToBeFunction = relevantTypeParamsString.contains(returnType) || hasTypeParameters.accept(returnType);
            
            String enclosingElementGenericQualifiedName = needsToBeFunction
                    ? elementGenericQualifiedName(enclosingElement)
                    : qualifiedName.apply(enclosingElement) + (relevantTypeParams.isEmpty() ? "" : "<" + mkString(", ", repeat("?", relevantTypeParams.size())) + ">");
            
            String modifiers = (options.makeFieldsPublic() ? "public" : resolveVisibility(field)) + " static final";
            String fieldName = field.getSimpleName().toString();
            
            String returnClause = "return " + (isPrivate ? "(" + returnType + ")" : "");
            
            Class<?> fieldClass = returnType.equals(Boolean.class.getName()) ? options.getPredicateClassForInstanceFields() : options.getClassForInstanceFields();
            String fundef = fieldClass.getName().replace('$', '.') + "<" + enclosingElementGenericQualifiedName + ", " + returnType + ">";
            String declaration = modifiers + " " + (needsToBeFunction ? relevantTypeParamsString + " ": "") + fundef + " " + fieldName;

            Iterable<String> tryBlock = isPrivate
                ? Some(returnClause + "$getMember().get($self);")
                : Some(returnClause + "$self." + fieldName + ";");
            
            Iterable<String> tryCatchBlock = isPrivate
                ? concat(
                    Some("try {"),
                    map(tryBlock, prepend("    ")),
                    catchBlock,
                    Some("}"))
                : tryBlock;
                        
            Iterable<String> applyBlock = concat(
                Some("protected " + returnType + " $do(" + enclosingElementGenericQualifiedName + " $self) {"),
                map(tryCatchBlock, prepend("    ")),
                Some("}")
            );
                        
            @SuppressWarnings("unchecked")
            Iterable<String> res = concat(
                needsToBeFunction
                    ? Some(declaration + "() { return new " + fundef + "() {")
                    : Some(declaration + " = new " + fundef + "() {"),
                isPrivate || options.generateMemberInitializerForFields()
                    ? concat(map(memberInitializer(enclosingElementGenericQualifiedName, fieldName, elementClass(field), Collections.<String>newList()), prepend("    ")),
                             EmptyLine)
                    : None,
                options.generateMemberAccessorForFields()
                    ? concat(map(memberAccessor(enclosingElementGenericQualifiedName, elementClass(field)), prepend("    ")),
                             EmptyLine)
                    : None,
                options.generateMemberNameAccessorForFields()
                    ? concat(map(memberNameAccessor(fieldName), prepend("    ")),
                             EmptyLine)
                    : None,
                map(options.getAdditionalBodyLinesForInstanceFields(), prepend("    ")),
                EmptyLine,
                isPrivate && (hasNonQmarkGenerics(returnType) || field.asType().getKind() == TypeKind.TYPEVAR)
                    ? Some("    @SuppressWarnings(\"unchecked\")")
                    : None,
                map(applyBlock, prepend("    ")),
                Some("};"),
                needsToBeFunction
                    ? Some("}")
                    : None,
                EmptyLine
            );
            return res;
        }
    };
}