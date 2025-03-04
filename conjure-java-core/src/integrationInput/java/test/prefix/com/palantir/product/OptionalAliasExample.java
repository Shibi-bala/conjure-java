package test.prefix.com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.errorprone.annotations.CheckReturnValue;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.Safe;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.Generated;

@Safe
@JsonDeserialize(builder = OptionalAliasExample.Builder.class)
@Generated("com.palantir.conjure.java.types.BeanGenerator")
public final class OptionalAliasExample {
    private final OptionalAlias optionalAlias;

    private OptionalAliasExample(OptionalAlias optionalAlias) {
        validateFields(optionalAlias);
        this.optionalAlias = optionalAlias;
    }

    @JsonProperty("optionalAlias")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public OptionalAlias getOptionalAlias() {
        return this.optionalAlias;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return this == other || (other instanceof OptionalAliasExample && equalTo((OptionalAliasExample) other));
    }

    private boolean equalTo(OptionalAliasExample other) {
        return this.optionalAlias.equals(other.optionalAlias);
    }

    @Override
    public int hashCode() {
        return this.optionalAlias.hashCode();
    }

    @Override
    @Safe
    public String toString() {
        return "OptionalAliasExample{optionalAlias: " + optionalAlias + '}';
    }

    public static OptionalAliasExample of(OptionalAlias optionalAlias) {
        return builder().optionalAlias(optionalAlias).build();
    }

    private static void validateFields(OptionalAlias optionalAlias) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, optionalAlias, "optionalAlias");
        if (missingFields != null) {
            throw new SafeIllegalArgumentException(
                    "Some required fields have not been set", SafeArg.of("missingFields", missingFields));
        }
    }

    private static List<String> addFieldIfMissing(List<String> prev, Object fieldValue, String fieldName) {
        List<String> missingFields = prev;
        if (fieldValue == null) {
            if (missingFields == null) {
                missingFields = new ArrayList<>(1);
            }
            missingFields.add(fieldName);
        }
        return missingFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Generated("com.palantir.conjure.java.types.BeanBuilderGenerator")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
        boolean _buildInvoked;

        private OptionalAlias optionalAlias = OptionalAlias.empty();

        private Builder() {}

        public Builder from(OptionalAliasExample other) {
            checkNotBuilt();
            optionalAlias(other.getOptionalAlias());
            return this;
        }

        @JsonSetter(value = "optionalAlias", nulls = Nulls.AS_EMPTY)
        public Builder optionalAlias(@Nonnull OptionalAlias optionalAlias) {
            checkNotBuilt();
            this.optionalAlias = Preconditions.checkNotNull(optionalAlias, "optionalAlias cannot be null");
            return this;
        }

        @CheckReturnValue
        public OptionalAliasExample build() {
            checkNotBuilt();
            this._buildInvoked = true;
            return new OptionalAliasExample(optionalAlias);
        }

        private void checkNotBuilt() {
            Preconditions.checkState(!_buildInvoked, "Build has already been called");
        }
    }
}
