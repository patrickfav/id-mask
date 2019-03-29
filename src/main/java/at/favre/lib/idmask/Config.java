package at.favre.lib.idmask;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Config {
    abstract String name();

    abstract int numberOfLegs();
}
