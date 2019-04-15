package at.favre.lib.idmask.ext;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class IdMaskJacksonTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testLongSerialization() throws IOException {
        testSerialization(new LongIdUser(67L, "pf"));
    }

    private <T> void testSerialization(T original) throws IOException {
        String out = mapper.writeValueAsString(original);
        System.out.println(out);
        T user = (T) mapper.readValue(out, original.getClass());
        assertEquals(original, user);
    }

    @Test
    public void testUuidSerialization() throws IOException {
        testSerialization(new UuidUser(UUID.randomUUID(), "pf"));
    }

    @Test
    public void testBigIntSerialization() throws IOException {
        testSerialization(new BigIntUser(BigInteger.valueOf(new Random().nextLong()), "pf"));
    }

    static class LongIdUser {
        @JsonSerialize(using = IdMaskJackson.LongSerializer.class)
        @JsonDeserialize(using = IdMaskJackson.LongDeserializer.class)
        private final long id;
        private final String username;

        @JsonCreator
        public LongIdUser(@JsonProperty("id") long id, @JsonProperty("username") String username) {
            this.id = id;
            this.username = username;
        }

        public long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LongIdUser that = (LongIdUser) o;
            return id == that.id &&
                    Objects.equals(username, that.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, username);
        }
    }

    static class UuidUser {
        @JsonSerialize(using = IdMaskJackson.UuidSerializer.class)
        @JsonDeserialize(using = IdMaskJackson.UuidDeserializer.class)
        private final UUID id;
        private final String username;

        @JsonCreator
        public UuidUser(@JsonProperty("id") UUID id, @JsonProperty("username") String username) {
            this.id = id;
            this.username = username;
        }

        public UUID getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UuidUser uuidUser = (UuidUser) o;
            return Objects.equals(id, uuidUser.id) &&
                    Objects.equals(username, uuidUser.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, username);
        }
    }

    static class BigIntUser {
        @JsonSerialize(using = IdMaskJackson.BigIntegerSerializer.class)
        @JsonDeserialize(using = IdMaskJackson.BigIntegerDeserializer.class)
        private final BigInteger id;
        private final String username;

        @JsonCreator
        public BigIntUser(@JsonProperty("id") BigInteger id, @JsonProperty("username") String username) {
            this.id = id;
            this.username = username;
        }

        public BigInteger getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BigIntUser uuidUser = (BigIntUser) o;
            return Objects.equals(id, uuidUser.id) &&
                    Objects.equals(username, uuidUser.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, username);
        }
    }
}
