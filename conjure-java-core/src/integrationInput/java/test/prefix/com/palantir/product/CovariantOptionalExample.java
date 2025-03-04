package test.prefix.com.palantir.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.errorprone.annotations.CheckReturnValue;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.Generated;

@JsonDeserialize(builder = CovariantOptionalExample.Builder.class)
@Generated("com.palantir.conjure.java.types.BeanGenerator")
public final class CovariantOptionalExample {
    private final Optional<Object> item;

    private final Optional<Set<StringAliasExample>> setItem;

    private int memoizedHashCode;

    private CovariantOptionalExample(Optional<Object> item, Optional<Set<StringAliasExample>> setItem) {
        validateFields(item, setItem);
        this.item = item;
        this.setItem = setItem;
    }

    @JsonProperty("item")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public Optional<Object> getItem() {
        return this.item;
    }

    @JsonProperty("setItem")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public Optional<Set<StringAliasExample>> getSetItem() {
        return this.setItem;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return this == other
                || (other instanceof CovariantOptionalExample && equalTo((CovariantOptionalExample) other));
    }

    private boolean equalTo(CovariantOptionalExample other) {
        if (this.memoizedHashCode != 0
                && other.memoizedHashCode != 0
                && this.memoizedHashCode != other.memoizedHashCode) {
            return false;
        }
        return this.item.equals(other.item) && this.setItem.equals(other.setItem);
    }

    @Override
    public int hashCode() {
        int result = memoizedHashCode;
        if (result == 0) {
            int hash = 1;
            hash = 31 * hash + this.item.hashCode();
            hash = 31 * hash + this.setItem.hashCode();
            result = hash;
            memoizedHashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return "CovariantOptionalExample{item: " + item + ", setItem: " + setItem + '}';
    }

    public static CovariantOptionalExample of(Object item, Set<StringAliasExample> setItem) {
        return builder().item(Optional.of(item)).setItem(Optional.of(setItem)).build();
    }

    private static void validateFields(Optional<Object> item, Optional<Set<StringAliasExample>> setItem) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, item, "item");
        missingFields = addFieldIfMissing(missingFields, setItem, "setItem");
        if (missingFields != null) {
            throw new SafeIllegalArgumentException(
                    "Some required fields have not been set", SafeArg.of("missingFields", missingFields));
        }
    }

    private static List<String> addFieldIfMissing(List<String> prev, Object fieldValue, String fieldName) {
        List<String> missingFields = prev;
        if (fieldValue == null) {
            if (missingFields == null) {
                missingFields = new ArrayList<>(2);
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

        private Optional<Object> item = Optional.empty();

        private Optional<Set<StringAliasExample>> setItem = Optional.empty();

        private Builder() {}

        public Builder from(CovariantOptionalExample other) {
            checkNotBuilt();
            item(other.getItem());
            setItem(other.getSetItem());
            return this;
        }

        @JsonSetter(value = "item", nulls = Nulls.SKIP)
        public Builder item(@Nonnull Optional<?> item) {
            checkNotBuilt();
            this.item = Preconditions.checkNotNull(item, "item cannot be null").map(Function.identity());
            return this;
        }

        public Builder item(@Nonnull Object item) {
            checkNotBuilt();
            this.item = Optional.of(Preconditions.checkNotNull(item, "item cannot be null"));
            return this;
        }

        @JsonSetter(value = "setItem", nulls = Nulls.SKIP)
        public Builder setItem(@Nonnull Optional<? extends Set<StringAliasExample>> setItem) {
            checkNotBuilt();
            this.setItem = Preconditions.checkNotNull(setItem, "setItem cannot be null")
                    .map(Function.identity());
            return this;
        }

        public Builder setItem(@Nonnull Set<StringAliasExample> setItem) {
            checkNotBuilt();
            this.setItem = Optional.of(Preconditions.checkNotNull(setItem, "setItem cannot be null"));
            return this;
        }

        @CheckReturnValue
        public CovariantOptionalExample build() {
            checkNotBuilt();
            this._buildInvoked = true;
            return new CovariantOptionalExample(item, setItem);
        }

        private void checkNotBuilt() {
            Preconditions.checkState(!_buildInvoked, "Build has already been called");
        }
    }
}
