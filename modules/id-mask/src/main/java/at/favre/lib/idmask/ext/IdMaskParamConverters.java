package at.favre.lib.idmask.ext;

import at.favre.lib.idmask.IdMask;

import javax.ws.rs.ext.ParamConverter;
import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

/**
 * A collection of default implementations for JAX-RS 2 ParamConverter for converting query-, path-, matrix-, header-, cookie- and form-parameter.
 * The JAX-RS dependency is optional so you have to add them to maven yourself if you want to use this class.
 * The following modules are required:
 * <ul>
 * <li>javax.ws.rs:javax.ws.rs-api</li>
 * </ul>
 * <p>
 * Tested with version 2.2.1 at the time of writing (2019).
 * <p>
 * Of course you require a JAX-RS implementation like Jersey or Apache-CXF.
 * <p>
 * Use this in a ParamConverterProvider like so:
 *
 * <pre>
 * &#64;Provider
 * public static class MyParamConverterProvider implements ParamConverterProvider {
 *      Inject private IdMask&lt;Long&gt; idMask;
 *
 *      &#64;Override
 *      public &lt;T&gt; ParamConverter&lt;T&gt; getConverter(Class&lt;T&gt; aClass, Type type, Annotation[] annotations) {
 *          if (aClass.equals(MaskedLongId.class)) {
 *              return (ParamConverter&lt;T&gt;) new IdMaskMaskedLongIdParamConverter(idMask);
 *          } else if (aClass.equals(Long.class)) {
 *              return (ParamConverter&lt;T&gt;) new IdMaskLongIdParamConverter(idMask);
 *          }
 *          ...
 *      }
 * }
 * </pre>
 * <p>
 * See here for more info: https://dzone.com/articles/using-parameter-converters-in-jax-rs
 */
@SuppressWarnings("WeakerAccess")
public final class IdMaskParamConverters {

    /**
     * ParamConverter for {@link MaskedLongId}
     */
    public static class IdMaskMaskedLongIdParamConverter extends BaseIdMaskParamConverter<Long, MaskedLongId> {
        /**
         * Create new instance
         *
         * @param idMask to use in this converter
         */
        public IdMaskMaskedLongIdParamConverter(IdMask<Long> idMask) {
            super(idMask);
        }

        @Override
        protected MaskedLongId _fromString(IdMask<Long> idMask, String s) {
            return new MaskedLongId(idMask.unmask(s));
        }

        @Override
        protected String _toString(IdMask<Long> idMask, MaskedLongId id) {
            return idMask.mask(id.getId());
        }
    }

    /**
     * ParamConverter for {@link Long}
     */
    public static class IdMaskLongIdParamConverter extends BaseIdMaskParamConverter<Long, Long> {
        /**
         * Create new instance
         *
         * @param idMask to use in this converter
         */
        public IdMaskLongIdParamConverter(IdMask<Long> idMask) {
            super(idMask);
        }

        @Override
        protected Long _fromString(IdMask<Long> idMask, String s) {
            return idMask.unmask(s);
        }

        @Override
        protected String _toString(IdMask<Long> idMask, Long id) {
            return idMask.mask(id);
        }
    }

    /**
     * ParamConverter for {@link UUID}
     */
    public static class IdMaskUuidParamConverter extends BaseIdMaskParamConverter<UUID, UUID> {
        /**
         * Create new instance
         *
         * @param idMask to use in this converter
         */
        public IdMaskUuidParamConverter(IdMask<UUID> idMask) {
            super(idMask);
        }

        @Override
        protected UUID _fromString(IdMask<UUID> idMask, String s) {
            return idMask.unmask(s);
        }

        @Override
        protected String _toString(IdMask<UUID> idMask, UUID id) {
            return idMask.mask(id);
        }
    }

    /**
     * ParamConverter for {@link IdMaskBigIntegerParamConverter}
     */
    public static class IdMaskBigIntegerParamConverter extends BaseIdMaskParamConverter<BigInteger, BigInteger> {
        /**
         * Create new instance
         *
         * @param idMask to use in this converter
         */
        public IdMaskBigIntegerParamConverter(IdMask<BigInteger> idMask) {
            super(idMask);
        }

        @Override
        protected BigInteger _fromString(IdMask<BigInteger> idMask, String s) {
            return idMask.unmask(s);
        }

        @Override
        protected String _toString(IdMask<BigInteger> idMask, BigInteger id) {
            return idMask.mask(id);
        }
    }

    /**
     * Base id mask param converter with default null handling.
     *
     * @param <T> type used by id mask instance
     * @param <P> external returned/consumed type
     */
    public abstract static class BaseIdMaskParamConverter<T, P> implements ParamConverter<P> {

        private final IdMask<T> idMask;

        protected BaseIdMaskParamConverter(IdMask<T> idMask) {
            this.idMask = idMask;
        }

        @Override
        public P fromString(String s) {
            if (s == null) {
                return null;
            }
            return _fromString(idMask, s);
        }

        protected abstract P _fromString(IdMask<T> idMask, String s);

        @Override
        public String toString(P id) {
            if (id == null) {
                return null;
            }
            return _toString(idMask, id);
        }

        protected abstract String _toString(IdMask<T> idMask, P id);
    }

    /**
     * Simple wrapper for type long to make it easier to differentiate between any
     * innocent long type and a masked id. The internal value cannot be null.
     */
    public static final class MaskedLongId {
        private final long id;

        /**
         * Create new instance
         *
         * @param id to set
         */
        public MaskedLongId(long id) {
            this.id = id;
        }

        /**
         * Get wrapped long value. This is the raw id, not the masked one.
         *
         * @return long
         */
        public long getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MaskedLongId that = (MaskedLongId) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return "MaskedLongId{" +
                    "id=" + id +
                    '}';
        }
    }
}
