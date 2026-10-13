package com.chaos.ranks.command;

import java.util.Collection;

import com.chaos.ranks.Lang;
import com.chaos.ranks.badge.BadgeRenderer;
import com.chaos.ranks.rank.RankDef;
import com.chaos.ranks.rank.RankStore;
import com.chaos.ranks.rank.RankTeams;
import com.chaos.ranks.server.RankServer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

/**
 * /rank                         abre el menú
 * /rank give <jugadores> <rango>
 * /rank remove <jugadores>
 * /rank delete <rango>
 */
public final class RankCommand {

	private static final DynamicCommandExceptionType UNKNOWN_RANK =
			new DynamicCommandExceptionType(id -> Lang.tr("msg.unknown_rank", id));

	private static final SuggestionProvider<CommandSourceStack> RANKS = (ctx, builder) ->
			SharedSuggestionProvider.suggest(RankStore.ranks().stream().map(d -> d.id), builder);

	private RankCommand() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("rank")
				.requires(source -> source.hasPermission(2))
				.executes(ctx -> {
					RankServer.open(ctx.getSource().getPlayerOrException(), "");
					return 1;
				})
				.then(Commands.literal("give")
						.then(Commands.argument("targets", EntityArgument.players())
								.then(Commands.argument("rank", StringArgumentType.word())
										.suggests(RANKS)
										.executes(RankCommand::give))))
				.then(Commands.literal("remove")
						.then(Commands.argument("targets", EntityArgument.players())
								.executes(RankCommand::remove)))
				.then(Commands.literal("delete")
						.then(Commands.argument("rank", StringArgumentType.word())
								.suggests(RANKS)
								.executes(RankCommand::delete))));
	}

	private static RankDef rank(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		String id = StringArgumentType.getString(ctx, "rank");
		RankDef def = RankStore.get(id);
		if (def == null) {
			throw UNKNOWN_RANK.create(id);
		}
		return def;
	}

	private static int give(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		RankDef def = rank(ctx);
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
  try { RankStore.assignAll(targets.stream().map(ServerPlayer::getUUID).toList(), def.id); }
  catch (RankStore.StorageException e) { throw new SimpleCommandExceptionType(Lang.tr("msg.storage_error")).create(); }
		RankTeams.sync(ctx.getSource().getServer());
		ctx.getSource().sendSuccess(() -> Lang.tr("msg.given", BadgeRenderer.build(def), targets.size()), true);
		return targets.size();
	}

	private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
  try { RankStore.assignAll(targets.stream().map(ServerPlayer::getUUID).toList(), null); }
  catch (RankStore.StorageException e) { throw new SimpleCommandExceptionType(Lang.tr("msg.storage_error")).create(); }
		RankTeams.sync(ctx.getSource().getServer());
		ctx.getSource().sendSuccess(() -> Lang.tr("msg.removed", targets.size()), true);
		return targets.size();
	}

	private static int delete(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		RankDef def = rank(ctx);
  try { RankStore.delete(def.id); }
  catch (RankStore.StorageException e) { throw new SimpleCommandExceptionType(Lang.tr("msg.storage_error")).create(); }
		RankTeams.sync(ctx.getSource().getServer());
		ctx.getSource().sendSuccess(() -> Lang.tr("msg.deleted", def.id), true);
		return 1;
	}
}
