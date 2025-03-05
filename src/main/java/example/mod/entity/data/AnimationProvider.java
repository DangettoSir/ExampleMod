package example.mod.entity.data;

import software.bernie.geckolib.core.animation.RawAnimation;


@FunctionalInterface
public interface AnimationProvider {
    RawAnimation getAnimation(boolean isMoving);
}
