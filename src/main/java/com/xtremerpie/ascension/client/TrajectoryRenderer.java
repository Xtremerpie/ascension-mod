package com.xtremerpie.ascension.client;

import com.xtremerpie.ascension.config.AscensionConfig;
import com.xtremerpie.ascension.physics.TrajectoryCalculator;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Draws the estimated projectile path in world-space (spec section 10:
 * "an optional trajectory renderer... render a subtle line... representing
 * the estimated projectile trajectory").
 *
 * CONFIDENCE NOTE — READ BEFORE DEBUGGING BUILD ERRORS HERE FIRST:
 * this is the single highest-risk file in the whole project. Minecraft's
 * immediate-mode rendering/vertex-consumer API (how you actually push a
 * vertex to the GPU: which methods exist on {@code VertexConsumer}, in
 * what order, whether a trailing call is still required to submit a
 * vertex) has changed shape across recent versions more than almost
 * anything else in the game, and it could not be checked against the real
 * 1.21.11 client classes in this environment (no game jar access — see
 * IMPLEMENTATION_STATUS.md). The code below reflects the vertex-consumer
 * pattern that has been standard across the 1.20-1.21 Fabric era
 * (consumers().getBuffer(RenderLayer.getLines()), then
 * .vertex(matrix, x, y, z), .color(...), .normal(...)). If this file
 * fails to compile, check the current VertexConsumer interface first —
 * everything else in this class (path sampling, when to draw, math) is
 * independent of that API and should still be correct even if the exact
 * draw calls need adjusting.
 */
public final class TrajectoryRenderer {

    private final TrajectoryCalculator calculator = new TrajectoryCalculator();

    public void render(WorldRenderContext context, PersistentProjectileEntity projectile) {
        AscensionConfig cfg = AscensionConfig.get();
        if (!cfg.trajectoryVisualizationEnabled) return;

        List<Vec3d> points = calculator.computePath(
                projectile.getWorld(),
                projectile.getPos(),
                projectile.getVelocity(),
                cfg.trajectoryMaxSteps,
                cfg.trajectorySampleEveryNTicks
        );
        if (points.size() < 2) return;

        Vec3d cameraPos = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (matrices == null || consumers == null) return;

        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());
        var matrix = matrices.peek().getPositionMatrix();

        // Cyan accent color matching the HUD's visual language, alpha
        // fading toward the end of the (bounded, short) predicted path.
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d a = points.get(i).subtract(cameraPos);
            Vec3d b = points.get(i + 1).subtract(cameraPos);
            float alpha = 1.0f - (float) i / points.size();

            buffer.vertex(matrix, (float) a.x, (float) a.y, (float) a.z)
                    .color(0.2f, 0.85f, 0.75f, alpha)
                    .normal(0, 1, 0);
            buffer.vertex(matrix, (float) b.x, (float) b.y, (float) b.z)
                    .color(0.2f, 0.85f, 0.75f, alpha)
                    .normal(0, 1, 0);
        }

        consumers.draw(RenderLayer.getLines());
    }
}
