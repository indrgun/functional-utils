package fi.solita.utils.functional.lens;

import static fi.solita.utils.functional.Collections.newList;
import static fi.solita.utils.functional.Collections.newSet;
import static fi.solita.utils.functional.Collections.newSortedSet;
import static fi.solita.utils.functional.Functional.map;
import static fi.solita.utils.functional.Option.Some;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import fi.solita.utils.functional.Apply;
import fi.solita.utils.functional.ApplyBi;
import fi.solita.utils.functional.Either;
import fi.solita.utils.functional.Function;
import fi.solita.utils.functional.Function2;
import fi.solita.utils.functional.FunctionalM;
import fi.solita.utils.functional.Option;
import fi.solita.utils.functional.Tuple;
import fi.solita.utils.functional.Tuple._1;
import fi.solita.utils.functional.Tuple._10;
import fi.solita.utils.functional.Tuple._11;
import fi.solita.utils.functional.Tuple._12;
import fi.solita.utils.functional.Tuple._13;
import fi.solita.utils.functional.Tuple._14;
import fi.solita.utils.functional.Tuple._15;
import fi.solita.utils.functional.Tuple._16;
import fi.solita.utils.functional.Tuple._17;
import fi.solita.utils.functional.Tuple._18;
import fi.solita.utils.functional.Tuple._19;
import fi.solita.utils.functional.Tuple._2;
import fi.solita.utils.functional.Tuple._20;
import fi.solita.utils.functional.Tuple._21;
import fi.solita.utils.functional.Tuple._22;
import fi.solita.utils.functional.Tuple._23;
import fi.solita.utils.functional.Tuple._24;
import fi.solita.utils.functional.Tuple._25;
import fi.solita.utils.functional.Tuple._26;
import fi.solita.utils.functional.Tuple._27;
import fi.solita.utils.functional.Tuple._28;
import fi.solita.utils.functional.Tuple._29;
import fi.solita.utils.functional.Tuple._3;
import fi.solita.utils.functional.Tuple._30;
import fi.solita.utils.functional.Tuple._31;
import fi.solita.utils.functional.Tuple._32;
import fi.solita.utils.functional.Tuple._4;
import fi.solita.utils.functional.Tuple._5;
import fi.solita.utils.functional.Tuple._6;
import fi.solita.utils.functional.Tuple._7;
import fi.solita.utils.functional.Tuple._8;
import fi.solita.utils.functional.Tuple._9;

public final class Lens<T,F> extends Setter<T,F> implements Apply<T,F> {
    private final Apply<? super T, F> getter;
    
    private Lens(Apply<? super T, F> getter, Function2<T,Apply<F,F>,T> setter) {
        super(setter);
        this.getter = getter;
    }

    public static final <T,F> Lens<T,F> of(final Apply<? super T, F> getter, final Builder<T> builder) {
        return new Lens<T,F>(getter, new Function2<T,Apply<F,F>,T>() {
            @Override
            public T apply(T t1, Apply<F, F> t2) {
                return builder.init(t1)
                        .with(getter, t2.apply(getter.apply(t1)))
                        .build();
            }
        });
    }
    
    public static final <T,F> Lens<T,F> of(Apply<? super T, F> getter, final ApplyBi<T, Apply<F,F>, T> setter) {
        return new Lens<T,F>(getter, new Function2<T,Apply<F,F>,T>() {
            @Override
            public T apply(T t1, Apply<F, F> t2) {
                return setter.apply(t1, t2);
            }
        });
    }
    
    public static final <D,E,S> Setter<D,S> eachCollection(final Setter<D,Collection<E>> lens, final Setter<E,S> lens2) {
        return eachIterable(lens, lens2, new Apply<Iterable<E>, Collection<E>>() {
            public Collection<E> apply(Iterable<E> t) {
                return newList(t);
            }
        });
    }
    
    public static final <D,E,S> Setter<D,S> eachList(final Setter<D,List<E>> lens, final Setter<E,S> lens2) {
        return eachIterable(lens, lens2, new Apply<Iterable<E>, List<E>>() {
            public List<E> apply(Iterable<E> t) {
                return newList(t);
            }
        });
    }
    
    public static final <D,E,S> Setter<D,S> eachSet(final Setter<D,Set<E>> lens, final Setter<E,S> lens2) {
        return eachIterable(lens, lens2, new Apply<Iterable<E>, Set<E>>() {
            public Set<E> apply(Iterable<E> t) {
                return newSet(t);
            }
        });
    }
    
    public static final <D,E,S> Setter<D,S> eachSortedSet(final Setter<D,SortedSet<E>> lens, final Setter<E,S> lens2) {
        return new Setter<D,S>(new Function2<D, Apply<S,S>, D>() {
            @Override
            public D apply(D d, final Apply<S, S> f) {
                return lens.modify(d, new Apply<SortedSet<E>,SortedSet<E>>() {
                    public SortedSet<E> apply(SortedSet<E> c) {
                        return c == null ? null : newSortedSet(c.comparator(), map(new Apply<E,E>() {
                            public E apply(E e) {
                                return lens2.modify(e, f);
                            }
                        }, c));
                    };
                });
            }
        });
    }
    
    public static final <D,E,S> Setter<D,S> eachIterable(final Setter<D,Iterable<E>> lens, final Setter<E,S> lens2) {
        return eachIterable(lens, lens2, Function.<Iterable<E>>id());
    }
    
    private static final <D,E,S,C extends Iterable<E>> Setter<D,S> eachIterable(final Setter<D,C> lens, final Setter<E,S> lens2, final Apply<Iterable<E>,C> wrap) {
        return new Setter<D,S>(new Function2<D, Apply<S,S>, D>() {
            @Override
            public D apply(D d, final Apply<S, S> f) {
                return lens.modify(d, new Apply<C,C>() {
                    public C apply(C c) {
                        return wrap.apply(map(new Apply<E,E>() {
                            public E apply(E e) {
                                return lens2.modify(e, f);
                            }
                        }, c));
                    };
                });
            }
        });
    }
    
    public static final <K,V> Lens<Map<K,V>,V> mapValue(final K key) {
        return new Lens<Map<K,V>, V>(new Apply<Map<K,V>, V>() {
            public V apply(Map<K, V> t) {
                return t.get(key);
            }
        }, new Function2<Map<K,V>, Apply<V,V>, Map<K,V>>() {
            @Override
            public Map<K, V> apply(Map<K, V> m, Apply<V, V> f) {
                return m.containsKey(key) ? FunctionalM.with(key, f.apply(m.get(key)), m) : m;
            }
        });
    }
    
    public F apply(T t) {
        return get(t);
    }

    public final F get(T t) {
        return getter.apply(t);
    }
    
    @Override
    public String toString() {
        return "(" + getter.toString() + "," + super.toString() + ")";
    }

    public final <O> Lens<T,O> andThen(final Lens<F,O> otherLens) {
        return new Lens<T,O>(new Apply<T,O>() {
            public O apply(T t) {
                return otherLens.get(get(t));
            }
        }, new Function2<T,Apply<O,O>,T>() {
            public T apply(T tt, final Apply<O,O> fo) {
                return modify(tt, new Apply<F,F>() {
                    public F apply(F f) {
                        return otherLens.modify(f, fo);
                    }
                });
            }
        });
    }
    
    public static final <T> Lens<T,T> id() {
        return Lens.of(Function.<T>id(), new ApplyBi<T, Apply<T,T>, T>() {
            public T apply(T t1, Apply<T, T> t2) {
                return t2.apply(t1);
            }
        });
    }
    
    public static final <L,R> Lens<Either<L,R>,Option<L>> left() {
        return new Lens<Either<L,R>, Option<L>>(new Apply<Either<L,R>, Option<L>>() {
            public Option<L> apply(Either<L, R> t) {
                return t.left;
            }
        }, new Function2<Either<L,R>, Apply<Option<L>,Option<L>>, Either<L,R>>() {
            @Override
            public Either<L, R> apply(Either<L, R> t, final Apply<Option<L>, Option<L>> f) {
                return t.first(new Apply<L, L>() {
                    public L apply(L t) {
                        return f.apply(Some(t)).get();
                    }
                });
            }
        });
    }
    
    public static final <L,R> Lens<Either<L,R>,Option<R>> right() {
        return new Lens<Either<L,R>, Option<R>>(new Apply<Either<L,R>, Option<R>>() {
            public Option<R> apply(Either<L, R> t) {
                return t.right;
            }
        }, new Function2<Either<L,R>, Apply<Option<R>,Option<R>>, Either<L,R>>() {
            @Override
            public Either<L, R> apply(Either<L, R> t, final Apply<Option<R>, Option<R>> f) {
                return t.second(new Apply<R, R>() {
                    public R apply(R t) {
                        return f.apply(Some(t)).get();
                    }
                });
            }
        });
    }
    
    public static final <T> Lens<_1<T>,T> _1() {
        return new Lens<_1<T>, T>(new Apply<_1<T>, T>() {
            public T apply(_1<T> t) {
                return t.get_1();
            }
        }, new Function2<_1<T>, Apply<T,T>, _1<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _1<T> apply(_1<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[0] = f.apply(t.get_1());
                return (_1<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_2<T>,T> _2() {
        return new Lens<_2<T>, T>(new Apply<_2<T>, T>() {
            public T apply(_2<T> t) {
                return t.get_2();
            }
        }, new Function2<_2<T>, Apply<T,T>, _2<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _2<T> apply(_2<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[1] = f.apply(t.get_2());
                return (_2<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_3<T>,T> _3() {
        return new Lens<_3<T>, T>(new Apply<_3<T>, T>() {
            public T apply(_3<T> t) {
                return t.get_3();
            }
        }, new Function2<_3<T>, Apply<T,T>, _3<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _3<T> apply(_3<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[2] = f.apply(t.get_3());
                return (_3<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_4<T>,T> _4() {
        return new Lens<_4<T>, T>(new Apply<_4<T>, T>() {
            public T apply(_4<T> t) {
                return t.get_4();
            }
        }, new Function2<_4<T>, Apply<T,T>, _4<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _4<T> apply(_4<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[3] = f.apply(t.get_4());
                return (_4<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_5<T>,T> _5() {
        return new Lens<_5<T>, T>(new Apply<_5<T>, T>() {
            public T apply(_5<T> t) {
                return t.get_5();
            }
        }, new Function2<_5<T>, Apply<T,T>, _5<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _5<T> apply(_5<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[4] = f.apply(t.get_5());
                return (_5<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_6<T>,T> _6() {
        return new Lens<_6<T>, T>(new Apply<_6<T>, T>() {
            public T apply(_6<T> t) {
                return t.get_6();
            }
        }, new Function2<_6<T>, Apply<T,T>, _6<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _6<T> apply(_6<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[5] = f.apply(t.get_6());
                return (_6<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_7<T>,T> _7() {
        return new Lens<_7<T>, T>(new Apply<_7<T>, T>() {
            public T apply(_7<T> t) {
                return t.get_7();
            }
        }, new Function2<_7<T>, Apply<T,T>, _7<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _7<T> apply(_7<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[6] = f.apply(t.get_7());
                return (_7<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_8<T>,T> _8() {
        return new Lens<_8<T>, T>(new Apply<_8<T>, T>() {
            public T apply(_8<T> t) {
                return t.get_8();
            }
        }, new Function2<_8<T>, Apply<T,T>, _8<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _8<T> apply(_8<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[7] = f.apply(t.get_8());
                return (_8<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_9<T>,T> _9() {
        return new Lens<_9<T>, T>(new Apply<_9<T>, T>() {
            public T apply(_9<T> t) {
                return t.get_9();
            }
        }, new Function2<_9<T>, Apply<T,T>, _9<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _9<T> apply(_9<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[8] = f.apply(t.get_9());
                return (_9<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_10<T>,T> _10() {
        return new Lens<_10<T>, T>(new Apply<_10<T>, T>() {
            public T apply(_10<T> t) {
                return t.get_10();
            }
        }, new Function2<_10<T>, Apply<T,T>, _10<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _10<T> apply(_10<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[9] = f.apply(t.get_10());
                return (_10<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_11<T>,T> _11() {
        return new Lens<_11<T>, T>(new Apply<_11<T>, T>() {
            public T apply(_11<T> t) {
                return t.get_11();
            }
        }, new Function2<_11<T>, Apply<T,T>, _11<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _11<T> apply(_11<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[10] = f.apply(t.get_11());
                return (_11<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_12<T>,T> _12() {
        return new Lens<_12<T>, T>(new Apply<_12<T>, T>() {
            public T apply(_12<T> t) {
                return t.get_12();
            }
        }, new Function2<_12<T>, Apply<T,T>, _12<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _12<T> apply(_12<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[11] = f.apply(t.get_12());
                return (_12<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_13<T>,T> _13() {
        return new Lens<_13<T>, T>(new Apply<_13<T>, T>() {
            public T apply(_13<T> t) {
                return t.get_13();
            }
        }, new Function2<_13<T>, Apply<T,T>, _13<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _13<T> apply(_13<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[12] = f.apply(t.get_13());
                return (_13<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_14<T>,T> _14() {
        return new Lens<_14<T>, T>(new Apply<_14<T>, T>() {
            public T apply(_14<T> t) {
                return t.get_14();
            }
        }, new Function2<_14<T>, Apply<T,T>, _14<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _14<T> apply(_14<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[13] = f.apply(t.get_14());
                return (_14<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_15<T>,T> _15() {
        return new Lens<_15<T>, T>(new Apply<_15<T>, T>() {
            public T apply(_15<T> t) {
                return t.get_15();
            }
        }, new Function2<_15<T>, Apply<T,T>, _15<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _15<T> apply(_15<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[14] = f.apply(t.get_15());
                return (_15<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_16<T>,T> _16() {
        return new Lens<_16<T>, T>(new Apply<_16<T>, T>() {
            public T apply(_16<T> t) {
                return t.get_16();
            }
        }, new Function2<_16<T>, Apply<T,T>, _16<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _16<T> apply(_16<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[15] = f.apply(t.get_16());
                return (_16<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_17<T>,T> _17() {
        return new Lens<_17<T>, T>(new Apply<_17<T>, T>() {
            public T apply(_17<T> t) {
                return t.get_17();
            }
        }, new Function2<_17<T>, Apply<T,T>, _17<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _17<T> apply(_17<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[16] = f.apply(t.get_17());
                return (_17<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_18<T>,T> _18() {
        return new Lens<_18<T>, T>(new Apply<_18<T>, T>() {
            public T apply(_18<T> t) {
                return t.get_18();
            }
        }, new Function2<_18<T>, Apply<T,T>, _18<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _18<T> apply(_18<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[17] = f.apply(t.get_18());
                return (_18<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_19<T>,T> _19() {
        return new Lens<_19<T>, T>(new Apply<_19<T>, T>() {
            public T apply(_19<T> t) {
                return t.get_19();
            }
        }, new Function2<_19<T>, Apply<T,T>, _19<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _19<T> apply(_19<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[18] = f.apply(t.get_19());
                return (_19<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_20<T>,T> _20() {
        return new Lens<_20<T>, T>(new Apply<_20<T>, T>() {
            public T apply(_20<T> t) {
                return t.get_20();
            }
        }, new Function2<_20<T>, Apply<T,T>, _20<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _20<T> apply(_20<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[19] = f.apply(t.get_20());
                return (_20<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_21<T>,T> _21() {
        return new Lens<_21<T>, T>(new Apply<_21<T>, T>() {
            public T apply(_21<T> t) {
                return t.get_21();
            }
        }, new Function2<_21<T>, Apply<T,T>, _21<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _21<T> apply(_21<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[20] = f.apply(t.get_21());
                return (_21<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_22<T>,T> _22() {
        return new Lens<_22<T>, T>(new Apply<_22<T>, T>() {
            public T apply(_22<T> t) {
                return t.get_22();
            }
        }, new Function2<_22<T>, Apply<T,T>, _22<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _22<T> apply(_22<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[21] = f.apply(t.get_22());
                return (_22<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_23<T>,T> _23() {
        return new Lens<_23<T>, T>(new Apply<_23<T>, T>() {
            public T apply(_23<T> t) {
                return t.get_23();
            }
        }, new Function2<_23<T>, Apply<T,T>, _23<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _23<T> apply(_23<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[22] = f.apply(t.get_23());
                return (_23<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_24<T>,T> _24() {
        return new Lens<_24<T>, T>(new Apply<_24<T>, T>() {
            public T apply(_24<T> t) {
                return t.get_24();
            }
        }, new Function2<_24<T>, Apply<T,T>, _24<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _24<T> apply(_24<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[23] = f.apply(t.get_24());
                return (_24<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_25<T>,T> _25() {
        return new Lens<_25<T>, T>(new Apply<_25<T>, T>() {
            public T apply(_25<T> t) {
                return t.get_25();
            }
        }, new Function2<_25<T>, Apply<T,T>, _25<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _25<T> apply(_25<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[24] = f.apply(t.get_25());
                return (_25<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_26<T>,T> _26() {
        return new Lens<_26<T>, T>(new Apply<_26<T>, T>() {
            public T apply(_26<T> t) {
                return t.get_26();
            }
        }, new Function2<_26<T>, Apply<T,T>, _26<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _26<T> apply(_26<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[25] = f.apply(t.get_26());
                return (_26<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_27<T>,T> _27() {
        return new Lens<_27<T>, T>(new Apply<_27<T>, T>() {
            public T apply(_27<T> t) {
                return t.get_27();
            }
        }, new Function2<_27<T>, Apply<T,T>, _27<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _27<T> apply(_27<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[26] = f.apply(t.get_27());
                return (_27<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_28<T>,T> _28() {
        return new Lens<_28<T>, T>(new Apply<_28<T>, T>() {
            public T apply(_28<T> t) {
                return t.get_28();
            }
        }, new Function2<_28<T>, Apply<T,T>, _28<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _28<T> apply(_28<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[27] = f.apply(t.get_28());
                return (_28<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_29<T>,T> _29() {
        return new Lens<_29<T>, T>(new Apply<_29<T>, T>() {
            public T apply(_29<T> t) {
                return t.get_29();
            }
        }, new Function2<_29<T>, Apply<T,T>, _29<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _29<T> apply(_29<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[28] = f.apply(t.get_29());
                return (_29<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_30<T>,T> _30() {
        return new Lens<_30<T>, T>(new Apply<_30<T>, T>() {
            public T apply(_30<T> t) {
                return t.get_30();
            }
        }, new Function2<_30<T>, Apply<T,T>, _30<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _30<T> apply(_30<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[29] = f.apply(t.get_30());
                return (_30<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_31<T>,T> _31() {
        return new Lens<_31<T>, T>(new Apply<_31<T>, T>() {
            public T apply(_31<T> t) {
                return t.get_31();
            }
        }, new Function2<_31<T>, Apply<T,T>, _31<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _31<T> apply(_31<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[30] = f.apply(t.get_31());
                return (_31<T>) Tuple.of(vs);
            }
        });
    }
    
    public static final <T> Lens<_32<T>,T> _32() {
        return new Lens<_32<T>, T>(new Apply<_32<T>, T>() {
            public T apply(_32<T> t) {
                return t.get_32();
            }
        }, new Function2<_32<T>, Apply<T,T>, _32<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public _32<T> apply(_32<T> t, Apply<T, T> f) {
                Object[] vs = ((Tuple)t).toArray();
                vs[31] = f.apply(t.get_32());
                return (_32<T>) Tuple.of(vs);
            }
        });
    }
}