package com.chaos.ranks.net;

import java.util.List;

import com.chaos.ranks.ChaosRanks;
import com.chaos.ranks.rank.RankDef;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Paquetes del menú de rangos. Los rangos viajan como JSON (los mismos campos que ranks.json). */
public final class RankPayloads {

	static final Gson GSON = new Gson();

	private RankPayloads() {}

	/** Servidor -> cliente: abre (o refresca) el menú con todos los rangos y selecciona {@code selectId}. */
	public record OpenManager(String ranksJson, String selectId) implements CustomPacketPayload {

		public static final Type<OpenManager> TYPE = new Type<>(ChaosRanks.id("open_manager"));
		public static final StreamCodec<ByteBuf, OpenManager> CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, OpenManager::ranksJson,
				ByteBufCodecs.STRING_UTF8, OpenManager::selectId,
				OpenManager::new);

		public OpenManager(List<RankDef> ranks, String selectId) {
			this(GSON.toJson(ranks), selectId);
		}

		public List<RankDef> ranks() {
			return GSON.fromJson(ranksJson, new TypeToken<List<RankDef>>() {}.getType());
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Cliente -> servidor: guarda un rango. {@code originalId} vacío = rango nuevo. */
	public record Save(String rankJson, String originalId) implements CustomPacketPayload {

		public static final Type<Save> TYPE = new Type<>(ChaosRanks.id("save_rank"));
		public static final StreamCodec<ByteBuf, Save> CODEC = StreamCodec.composite(
				ByteBufCodecs.stringUtf8(4096), Save::rankJson,
				ByteBufCodecs.stringUtf8(64), Save::originalId,
				Save::new);

		public Save(RankDef def, String originalId) {
			this(GSON.toJson(def), originalId);
		}

		public RankDef rank() {
			return GSON.fromJson(rankJson, RankDef.class);
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Cliente -> servidor: borra un rango. */
	public record Delete(String id) implements CustomPacketPayload {

		public static final Type<Delete> TYPE = new Type<>(ChaosRanks.id("delete_rank"));
		public static final StreamCodec<ByteBuf, Delete> CODEC =
				ByteBufCodecs.stringUtf8(64).map(Delete::new, Delete::id);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
