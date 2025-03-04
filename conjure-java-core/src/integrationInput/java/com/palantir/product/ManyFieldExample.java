package com.palantir.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.errorprone.annotations.CheckReturnValue;
import com.palantir.conjure.java.lib.internal.ConjureCollections;
import com.palantir.logsafe.Preconditions;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.Generated;

@JsonDeserialize(builder = ManyFieldExample.Builder.class)
@Generated("com.palantir.conjure.java.types.BeanGenerator")
public final class ManyFieldExample {
    private final String string;

    private final int integer;

    private final double doubleValue;

    private final Optional<String> optionalItem;

    private final List<String> items;

    private final Set<String> set;

    private final Map<String, String> map;

    private final StringAliasExample alias;

    private int memoizedHashCode;

    private ManyFieldExample(
            String string,
            int integer,
            double doubleValue,
            Optional<String> optionalItem,
            List<String> items,
            Set<String> set,
            Map<String, String> map,
            StringAliasExample alias) {
        validateFields(string, optionalItem, items, set, map, alias);
        this.string = string;
        this.integer = integer;
        this.doubleValue = doubleValue;
        this.optionalItem = optionalItem;
        this.items = Collections.unmodifiableList(items);
        this.set = Collections.unmodifiableSet(set);
        this.map = Collections.unmodifiableMap(map);
        this.alias = alias;
    }

    /** docs for string field */
    @JsonProperty("string")
    public String getString() {
        return this.string;
    }

    /** docs for integer field */
    @JsonProperty("integer")
    public int getInteger() {
        return this.integer;
    }

    /** docs for doubleValue field */
    @JsonProperty("doubleValue")
    public double getDoubleValue() {
        return this.doubleValue;
    }

    /**
     * docs for optionalItem field
     *
     * @deprecated an optional field is deprecated
     */
    @JsonProperty("optionalItem")
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @Deprecated
    public Optional<String> getOptionalItem() {
        return this.optionalItem;
    }

    /** docs for items field with exciting character$ used by javapoet. */
    @JsonProperty("items")
    public List<String> getItems() {
        return this.items;
    }

    /** docs for set field */
    @JsonProperty("set")
    public Set<String> getSet() {
        return this.set;
    }

    /** @deprecated deprecation documentation. */
    @JsonProperty("map")
    @Deprecated
    public Map<String, String> getMap() {
        return this.map;
    }

    /**
     * docs for alias field
     *
     * @deprecated This field is deprecated.
     */
    @JsonProperty("alias")
    @Deprecated
    public StringAliasExample getAlias() {
        return this.alias;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return this == other || (other instanceof ManyFieldExample && equalTo((ManyFieldExample) other));
    }

    private boolean equalTo(ManyFieldExample other) {
        if (this.memoizedHashCode != 0
                && other.memoizedHashCode != 0
                && this.memoizedHashCode != other.memoizedHashCode) {
            return false;
        }
        return this.string.equals(other.string)
                && this.integer == other.integer
                && Double.doubleToLongBits(this.doubleValue) == Double.doubleToLongBits(other.doubleValue)
                && this.optionalItem.equals(other.optionalItem)
                && this.items.equals(other.items)
                && this.set.equals(other.set)
                && this.map.equals(other.map)
                && this.alias.equals(other.alias);
    }

    @Override
    public int hashCode() {
        int result = memoizedHashCode;
        if (result == 0) {
            int hash = 1;
            hash = 31 * hash + this.string.hashCode();
            hash = 31 * hash + this.integer;
            hash = 31 * hash + Double.hashCode(this.doubleValue);
            hash = 31 * hash + this.optionalItem.hashCode();
            hash = 31 * hash + this.items.hashCode();
            hash = 31 * hash + this.set.hashCode();
            hash = 31 * hash + this.map.hashCode();
            hash = 31 * hash + this.alias.hashCode();
            result = hash;
            memoizedHashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return "ManyFieldExample{string: " + string + ", integer: " + integer + ", doubleValue: " + doubleValue
                + ", optionalItem: " + optionalItem + ", items: " + items + ", set: " + set + ", map: " + map
                + ", alias: " + alias + '}';
    }

    private static void validateFields(
            String string,
            Optional<String> optionalItem,
            List<String> items,
            Set<String> set,
            Map<String, String> map,
            StringAliasExample alias) {
        List<String> missingFields = null;
        missingFields = addFieldIfMissing(missingFields, string, "string");
        missingFields = addFieldIfMissing(missingFields, optionalItem, "optionalItem");
        missingFields = addFieldIfMissing(missingFields, items, "items");
        missingFields = addFieldIfMissing(missingFields, set, "set");
        missingFields = addFieldIfMissing(missingFields, map, "map");
        missingFields = addFieldIfMissing(missingFields, alias, "alias");
        if (missingFields != null) {
            throw new SafeIllegalArgumentException(
                    "Some required fields have not been set", SafeArg.of("missingFields", missingFields));
        }
    }

    private static List<String> addFieldIfMissing(List<String> prev, Object fieldValue, String fieldName) {
        List<String> missingFields = prev;
        if (fieldValue == null) {
            if (missingFields == null) {
                missingFields = new ArrayList<>(6);
            }
            missingFields.add(fieldName);
        }
        return missingFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Generated("com.palantir.conjure.java.types.BeanBuilderGenerator")
    public static final class Builder {
        boolean _buildInvoked;

        private String string;

        private int integer;

        private double doubleValue;

        private Optional<String> optionalItem = Optional.empty();

        private List<String> items = ConjureCollections.newList();

        private Set<String> set = ConjureCollections.newSet();

        private Map<String, String> map = new LinkedHashMap<>();

        private StringAliasExample alias;

        private boolean _integerInitialized = false;

        private boolean _doubleValueInitialized = false;

        private Builder() {}

        public Builder from(ManyFieldExample other) {
            checkNotBuilt();
            string(other.getString());
            integer(other.getInteger());
            doubleValue(other.getDoubleValue());
            optionalItem(other.getOptionalItem());
            items(other.getItems());
            set(other.getSet());
            map(other.getMap());
            alias(other.getAlias());
            return this;
        }

        /** docs for string field */
        @JsonSetter("string")
        public Builder string(@Nonnull String string) {
            checkNotBuilt();
            this.string = Preconditions.checkNotNull(string, "string cannot be null");
            return this;
        }

        /** docs for integer field */
        @JsonSetter("integer")
        public Builder integer(int integer) {
            checkNotBuilt();
            this.integer = integer;
            this._integerInitialized = true;
            return this;
        }

        /** docs for doubleValue field */
        @JsonSetter("doubleValue")
        public Builder doubleValue(double doubleValue) {
            checkNotBuilt();
            this.doubleValue = doubleValue;
            this._doubleValueInitialized = true;
            return this;
        }

        /**
         * docs for optionalItem field
         *
         * @deprecated an optional field is deprecated
         */
        @Deprecated
        @JsonSetter(value = "optionalItem", nulls = Nulls.SKIP)
        public Builder optionalItem(@Nonnull Optional<String> optionalItem) {
            checkNotBuilt();
            this.optionalItem = Preconditions.checkNotNull(optionalItem, "optionalItem cannot be null");
            return this;
        }

        /**
         * docs for optionalItem field
         *
         * @deprecated an optional field is deprecated
         */
        @Deprecated
        public Builder optionalItem(@Nonnull String optionalItem) {
            checkNotBuilt();
            this.optionalItem = Optional.of(Preconditions.checkNotNull(optionalItem, "optionalItem cannot be null"));
            return this;
        }

        /** docs for items field with exciting character$ used by javapoet. */
        @JsonSetter(value = "items", nulls = Nulls.SKIP, contentNulls = Nulls.FAIL)
        public Builder items(@Nonnull Iterable<String> items) {
            checkNotBuilt();
            this.items = ConjureCollections.newNonNullList(Preconditions.checkNotNull(items, "items cannot be null"));
            return this;
        }

        /** docs for items field with exciting character$ used by javapoet. */
        public Builder addAllItems(@Nonnull Iterable<String> items) {
            checkNotBuilt();
            ConjureCollections.addAllAndCheckNonNull(
                    this.items, Preconditions.checkNotNull(items, "items cannot be null"));
            return this;
        }

        /** docs for items field with exciting character$ used by javapoet. */
        public Builder items(String items) {
            checkNotBuilt();
            Preconditions.checkNotNull(items, "items cannot be null");
            this.items.add(items);
            return this;
        }

        /** docs for set field */
        @JsonSetter(value = "set", nulls = Nulls.SKIP, contentNulls = Nulls.FAIL)
        public Builder set(@Nonnull Iterable<String> set) {
            checkNotBuilt();
            this.set = ConjureCollections.newNonNullSet(Preconditions.checkNotNull(set, "set cannot be null"));
            return this;
        }

        /** docs for set field */
        public Builder addAllSet(@Nonnull Iterable<String> set) {
            checkNotBuilt();
            ConjureCollections.addAllAndCheckNonNull(this.set, Preconditions.checkNotNull(set, "set cannot be null"));
            return this;
        }

        /** docs for set field */
        public Builder set(String set) {
            checkNotBuilt();
            Preconditions.checkNotNull(set, "set cannot be null");
            this.set.add(set);
            return this;
        }

        /** @deprecated deprecation documentation. */
        @Deprecated
        @JsonSetter(value = "map", nulls = Nulls.SKIP, contentNulls = Nulls.FAIL)
        public Builder map(@Nonnull Map<String, String> map) {
            checkNotBuilt();
            this.map = new LinkedHashMap<>(Preconditions.checkNotNull(map, "map cannot be null"));
            return this;
        }

        /** @deprecated deprecation documentation. */
        @Deprecated
        public Builder putAllMap(@Nonnull Map<String, String> map) {
            checkNotBuilt();
            this.map.putAll(Preconditions.checkNotNull(map, "map cannot be null"));
            return this;
        }

        /** @deprecated deprecation documentation. */
        @Deprecated
        public Builder map(String key, String value) {
            checkNotBuilt();
            this.map.put(key, value);
            return this;
        }

        /**
         * docs for alias field
         *
         * @deprecated This field is deprecated.
         */
        @Deprecated
        @JsonSetter("alias")
        public Builder alias(@Nonnull StringAliasExample alias) {
            checkNotBuilt();
            this.alias = Preconditions.checkNotNull(alias, "alias cannot be null");
            return this;
        }

        private void validatePrimitiveFieldsHaveBeenInitialized() {
            List<String> missingFields = null;
            missingFields = addFieldIfMissing(missingFields, _integerInitialized, "integer");
            missingFields = addFieldIfMissing(missingFields, _doubleValueInitialized, "doubleValue");
            if (missingFields != null) {
                throw new SafeIllegalArgumentException(
                        "Some required fields have not been set", SafeArg.of("missingFields", missingFields));
            }
        }

        private static List<String> addFieldIfMissing(List<String> prev, boolean initialized, String fieldName) {
            List<String> missingFields = prev;
            if (!initialized) {
                if (missingFields == null) {
                    missingFields = new ArrayList<>(2);
                }
                missingFields.add(fieldName);
            }
            return missingFields;
        }

        @CheckReturnValue
        public ManyFieldExample build() {
            checkNotBuilt();
            this._buildInvoked = true;
            validatePrimitiveFieldsHaveBeenInitialized();
            return new ManyFieldExample(string, integer, doubleValue, optionalItem, items, set, map, alias);
        }

        private void checkNotBuilt() {
            Preconditions.checkState(!_buildInvoked, "Build has already been called");
        }
    }
}
