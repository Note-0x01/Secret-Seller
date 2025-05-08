package company.fools.secretseller.mixin;

import company.fools.secretseller.Merchantry;
import company.fools.secretseller.SecretSeller;
import company.fools.secretseller.StateSaverAndLoader;
import company.fools.secretseller.mob.SellerManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.Spawner;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Final
    @Mutable
    @Shadow private List<Spawner> spawners;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void addManager(
            MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci
    ) {
        if(dimensionOptions.dimensionTypeEntry().matchesKey(DimensionTypes.OVERWORLD)) {
            List<Spawner> newList = new ArrayList<>(spawners);
            newList.add(SecretSeller.INSTANCE.getSellerManager());
            this.spawners = newList;
        }
    }
}
