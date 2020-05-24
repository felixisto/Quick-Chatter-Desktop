package quickchatter.network.basic;

import org.jetbrains.annotations.NotNull;

import quickchatter.utilities.Errors;

public class TransmissionType {
    public final @NotNull String value;

    public TransmissionType(@NotNull String value) throws Exception {
        if (value.isEmpty()) {
            Errors.throwInvalidArgument("Type cannot be empty");
        }

        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TransmissionType) {
            return hashCode() == other.hashCode();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
