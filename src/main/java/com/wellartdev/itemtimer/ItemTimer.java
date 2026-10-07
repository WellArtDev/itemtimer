package com.wellartdev.itemtimer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Floating despawn countdown above every dropped item.
 *
 * Dropped items despawn exactly 5 minutes (6000 ticks) after they hit the
 * ground, and nothing in vanilla tells you how long each stack has left.
 * ItemTimer draws a small nametag-style timer ("4:32") above every
 * {@link ItemEntity} in the world, colored white normally and switching to
 * red when under 60 seconds remain — so you always know which loot to
 * rescue first.
 *
 * <p>Rendering uses Fabric's {@code LevelRenderEvents.COLLECT_SUBMITS}
 * hook and submits the text through vanilla's own text submit path, with
 * the same camera-facing orientation and 0.025 scale vanilla nametags use.
 * (The old {@code WorldRenderEvents.AFTER_ENTITIES} / immediate-mode
 * {@code drawInBatch} approach no longer exists on this version.)
 *
 * <p>Client-side only. No mixins, no networking, nothing persisted.
 */
public class ItemTimer implements ClientModInitializer {
    public static final String MOD_ID = "itemtimer";

    /** Items despawn 5 minutes after being dropped (20 ticks per second). */
    private static final int DESPAWN_TICKS = 20 * 60 * 5;

    /** Red warning kicks in under this many seconds remaining. */
    private static final int URGENT_SECONDS = 60;

    private static final int COLOR_NORMAL = 0xFFFFFF;
    private static final int COLOR_URGENT = 0xFF5555;

    /** Full brightness so the timer stays readable in the dark. */
    private static final int FULL_BRIGHT_LIGHT = 15728880;

    /** Nametag text scale — matches vanilla nametag rendering. */
    private static final float TEXT_SCALE = 0.025F;

    /** Height above the item the timer floats at. */
    private static final double HOVER_HEIGHT = 0.75;

    @Override
    public void onInitializeClient() {
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level == null) {
                return;
            }

            Camera camera = mc.gameRenderer.mainCamera();
            Vec3 camPos = camera.position();
            float tickDelta = mc.getDeltaTracker().getGameTimeDeltaTicks();
            PoseStack poseStack = context.poseStack();

            for (Entity entity : level.entitiesForRendering()) {
                if (!(entity instanceof ItemEntity item)) {
                    continue;
                }

                // Remaining time until despawn, clamped at zero.
                int remainingTicks = Math.max(0, DESPAWN_TICKS - item.getAge());
                int remainingSeconds = remainingTicks / 20;
                String text = (remainingSeconds / 60) + ":"
                        + String.format("%02d", remainingSeconds % 60);
                int color = remainingSeconds < URGENT_SECONDS ? COLOR_URGENT : COLOR_NORMAL;

                // Camera-relative position so the pose matrix lands in world
                // space where the submit pipeline expects it.
                Vec3 pos = item.getPosition(tickDelta).subtract(camPos);

                poseStack.pushPose();
                poseStack.translate(pos.x, pos.y + HOVER_HEIGHT, pos.z);
                // Face the player, like a vanilla nametag.
                poseStack.mulPose(camera.rotation());
                poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

                float centeredX = -mc.font.width(text) / 2.0F;
                context.submitNodeCollector().submitText(
                        poseStack,
                        centeredX, 0.0F,
                        FormattedCharSequence.forward(text, Style.EMPTY),
                        false,
                        Font.DisplayMode.NORMAL,
                        color,
                        0,
                        FULL_BRIGHT_LIGHT,
                        0);
                poseStack.popPose();
            }
        });
    }
}
