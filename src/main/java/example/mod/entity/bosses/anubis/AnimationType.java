package example.mod.entity.bosses.anubis;

import example.mod.entity.data.AnimationProvider;
import software.bernie.geckolib.core.animation.RawAnimation;

public enum AnimationType implements AnimationProvider {
    IDLE(RawAnimation.begin().thenLoop("idle"), RawAnimation.begin().thenLoop("walk")),
    DASH(RawAnimation.begin().thenPlay("dash")),
    ATTACK_1(RawAnimation.begin().thenPlay("attack_1")),
    ATTACK_2(RawAnimation.begin().thenPlay("attack_2")),
    ATTACK_3(RawAnimation.begin().thenPlay("attack_3")),
    ATTACK_4(RawAnimation.begin().thenPlay("attack_4")),
    ATTACK_5(RawAnimation.begin().thenPlay("attack_5")),
    ATTACK_6(RawAnimation.begin().thenPlay("attack_6")),
    ATTACK_7(RawAnimation.begin().thenPlay("attack_7")),
    DEAD(RawAnimation.begin().thenPlay("dead"));

    private final RawAnimation animation;
    private final RawAnimation movingAnimation;

    AnimationType(RawAnimation animation, RawAnimation movingAnimation) {
        this.animation = animation;
        this.movingAnimation = movingAnimation;
    }

    AnimationType(RawAnimation animation) {
        this(animation, animation);
    }

    @Override
    public RawAnimation getAnimation(boolean isMoving) {
        return isMoving && movingAnimation != animation ? movingAnimation : animation;
    }
}