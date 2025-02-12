package dev.gether.getutils.models;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EffectData {
    int duration;
    int amplifier;
    boolean ambient;
    boolean particles;
    boolean icon;
}
