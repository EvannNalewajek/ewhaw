package com.minecraft.mod.ewhaw.world;

import com.minecraft.mod.ewhaw.entity.SqwackEntity;
import com.minecraft.mod.ewhaw.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SqwackSpawner {
    private int spawnDelay = 2400; // 2 minutes par défaut entre chaque tentative

    public void tick(ServerLevel level) {
        if (--this.spawnDelay <= 0) {
            this.spawnDelay = 12000 + level.random.nextInt(12000); // 10 à 20 minutes entre chaque spawn réussi ou échec
            
            if (level.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING) && level.isDay()) {
                // On vérifie si Sqwack est déjà là
                List<? extends SqwackEntity> sqwacks = level.getEntities(ModEntityTypes.SQWACK.get(), e -> true);
                if (sqwacks.isEmpty()) {
                    spawnSqwack(level);
                }
            }
        }
    }

    private void spawnSqwack(ServerLevel level) {
        ServerPlayer player = level.getRandomPlayer();
        if (player == null) return;

        RandomSource random = level.random;
        
        // On essaie de spawn derrière le joueur
        // On prend le vecteur de direction opposé à là où regarde le joueur
        Vec3 lookAngle = player.getLookAngle().reverse();
        double distance = 15.0D + random.nextDouble() * 10.0D;
        
        double spawnX = player.getX() + lookAngle.x * distance + (random.nextDouble() - 0.5D) * 5.0D;
        double spawnZ = player.getZ() + lookAngle.z * distance + (random.nextDouble() - 0.5D) * 5.0D;
        
        BlockPos pos = new BlockPos((int)spawnX, (int)player.getY(), (int)spawnZ);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        pos = new BlockPos(pos.getX(), y, pos.getZ());

        if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
            SqwackEntity sqwack = ModEntityTypes.SQWACK.get().create(level);
            if (sqwack != null) {
                sqwack.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
                sqwack.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
                level.addFreshEntity(sqwack);
            }
        }
    }
}
