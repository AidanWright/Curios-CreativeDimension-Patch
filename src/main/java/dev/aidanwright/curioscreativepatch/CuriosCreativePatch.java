package dev.aidanwright.curioscreativepatch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Command;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CuriosCreativePatch.MODID)
public class CuriosCreativePatch
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "curioscreativepatch";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CuriosCreativePatch(IEventBus modEventBus, ModContainer modContainer)
    {
    }

    @EventBusSubscriber(modid = MODID)
    public class CuriosCommandExtension {

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
            CommandNode<CommandSourceStack> root = dispatcher.getRoot();
            CommandNode<CommandSourceStack> curios = root.getChild("curios");
            if (!(curios instanceof LiteralCommandNode<?>)) return;

            // Build "/curios save <player>"
            LiteralCommandNode<CommandSourceStack> getNode = Commands
                    .literal("save")
                    .then(Commands.argument("player", EntityArgument.player())
                            .executes(ctx -> executeSave(
                                    ctx.getSource(),
                                    EntityArgument.getPlayer(ctx, "player")
                            ))
                    )
                    .build();

            curios.addChild(getNode);

            // Build "/curios load <player>"
            LiteralCommandNode<CommandSourceStack> setNode = Commands
                    .literal("load")
                    .then(Commands.argument("player", EntityArgument.player())
                            .executes(ctx -> executeLoad(
                                    ctx.getSource(),
                                    EntityArgument.getPlayer(ctx, "player")
                            ))
                    )
                    .build();

            curios.addChild(setNode);
        }


        private static int executeSave(CommandSourceStack src, ServerPlayer player) {

            // get handler optional.
            var optionalHandler = CuriosApi.getCuriosInventory(player);
            if (optionalHandler.isEmpty()) {
                src.sendFailure(Component.literal("Player's curios inventory not found."));
                return 0;
            }

            // exists so safe--hopefully.
            ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).get();
            // also clears (curios) inventory
            ListTag curiosList = handler.saveInventory(true);

            CompoundTag data = player.getPersistentData();
            // unlikely to collide--hopefully.
            data.put("curiosCreativeBackup", curiosList.copy());

            src.sendSuccess(() -> Component.literal("Curios saved to your player data."), false);
            return Command.SINGLE_SUCCESS;
        }

        private static int executeLoad(CommandSourceStack src, ServerPlayer player) {
            // get handler optional.
            var optionalHandler = CuriosApi.getCuriosInventory(player);
            if (optionalHandler.isEmpty()) {
                src.sendFailure(Component.literal("Player's curios inventory not found."));
                return 0;
            }

            // exists so safe--hopefully.
            ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).get();

            CompoundTag data = player.getPersistentData();
            // 10 == Tag.TAG_COMPOUND
            ListTag curiosBackup = data.getList("curiosCreativeBackup", 10);
            if (curiosBackup.isEmpty()) {
                src.sendFailure(Component.literal("No curios backup found."));
                return 0;
            }
            handler.loadInventory(curiosBackup);
            // clear up some space :)
            data.remove("curiosCreativeBackup");
            src.sendSuccess(() -> Component.literal("Loaded curios from backup."), false);
            return Command.SINGLE_SUCCESS;
        }
    }
}
