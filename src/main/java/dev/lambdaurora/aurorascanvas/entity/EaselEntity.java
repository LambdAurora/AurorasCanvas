/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.entity;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.AurorasCanvasSoundEvents;
import dev.lambdaurora.aurorascanvas.canvas.IndexedCanvas;
import dev.lambdaurora.aurorascanvas.network.CanvasOpenGuiPayload;
import dev.lambdaurora.aurorascanvas.util.Utils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry.*;

/**
 * Represents an easel minecart.
 * <p>
 * It can hold a canvas for display and interaction.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public class EaselEntity extends LivingEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(EaselEntity.class);
	private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(EaselEntity.class, EntityDataSerializers.ITEM_STACK);
	private static final String ITEM_KEY = "item";
	private static final String INVISIBLE_KEY = "invisible";
	private static final String FIXED_KEY = "fixed";

	private static final Predicate<Entity> RIDABLE_MINECARTS = entity -> entity instanceof AbstractMinecart minecart
			&& minecart.getMinecartType() == AbstractMinecart.Type.RIDEABLE;

	private boolean fixed;

	/**
	 * After punching the easel, the cooldown (in ticks) before you can punch it again without breaking it.
	 */
	public long lastHit;

	public EaselEntity(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return createLivingAttributes().add(Attributes.STEP_HEIGHT, 0.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ITEM, ItemStack.EMPTY);
	}

	public ItemStack getItem() {
		return this.getEntityData().get(DATA_ITEM);
	}

	public void setItem(ItemStack stack) {
		this.setItem(stack, true);
	}

	public void setItem(ItemStack stack, boolean updateNeighbours) {
		if (!stack.isEmpty()) {
			stack = stack.copyWithCount(1);
		}

		//this.onItemChanged(canvas);
		this.getEntityData().set(DATA_ITEM, stack);
		if (!stack.isEmpty()) {
			//this.playSound(this.getAddItemSound(), 1.0F, 1.0F);
		}

		if (updateNeighbours) {
			this.level().updateNeighbourForOutputSignal(this.blockPosition(), Blocks.AIR);
		}
	}

	private boolean hasPhysics() {
		return !this.isNoGravity() && !this.fixed;
	}

	/* Serialization */

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		CompoundTag itemNbt = nbt.getCompound(ITEM_KEY);
		if (!itemNbt.isEmpty()) {
			ItemStack stack = ItemStack.parseOptional(this.registryAccess(), itemNbt);
			if (stack.isEmpty()) {
				LOGGER.warn("Unable to load item from: {}", itemNbt);
			}

			this.setItem(stack, false);
		}

		this.setInvisible(nbt.getBoolean(INVISIBLE_KEY));
		this.fixed = nbt.getBoolean(FIXED_KEY);

		this.noPhysics = !this.hasPhysics();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		if (!this.getItem().isEmpty()) {
			nbt.put(ITEM_KEY, this.getItem().saveOptional(this.registryAccess()));
		}
		nbt.putBoolean(INVISIBLE_KEY, this.isInvisible());
		nbt.putBoolean(FIXED_KEY, this.fixed);
	}

	/* Movement */

	@Override
	public void travel(Vec3 travelVector) {
		if (this.hasPhysics()) {
			super.travel(travelVector);
		}
	}

	@Override
	public void setYBodyRot(float yBodyRot) {
		this.yBodyRotO = this.yRotO = yBodyRot;
		this.yHeadRotO = this.yHeadRot = yBodyRot;
	}

	@Override
	public void setYHeadRot(float yHeadRot) {
		this.yBodyRotO = this.yRotO = yHeadRot;
		this.yHeadRotO = this.yHeadRot = yHeadRot;
	}

	@Override
	protected float tickHeadTurn(float yRot, float animStep) {
		this.yBodyRotO = this.yRotO;
		this.yBodyRot = this.getYRot();
		return 0.f;
	}

	/* Interaction */

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	protected void pushEntities() {
		List<Entity> minecarts = this.level().getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS);

		for (Entity minecart : minecarts) {
			if (this.distanceToSqr(minecart) <= 0.2) {
				minecart.push(this);
			}
		}
	}

	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	public void submit(ServerPlayer player, IndexedCanvas canvas) {
		if (this.fixed || this.isRemoved() || this.getItem().isEmpty()) return;

		var canvasStack = this.getItem();
		var nbt = Utils.getOrCreateBlockEntityNbt(canvasStack, CANVAS_BLOCK_ENTITY_TYPE);
		canvas.writeNbt(nbt);
		Utils.writeBlockEntityNbtToStack(canvasStack, CANVAS_BLOCK_ENTITY_TYPE, nbt, false);
		this.setItem(canvasStack);

		this.gameEvent(GameEvent.BLOCK_CHANGE, player);

		for (var hand : InteractionHand.values()) {
			var handStack = player.getItemInHand(hand);

			if (handStack.is(PAINTER_PALETTE_ITEM)) {
				DRAW_ON_CANVAS_TRIGGER.trigger(player, handStack);
				break;
			}
		}
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		ItemStack handStack = player.getItemInHand(hand);
		boolean handStackValid = handStack.is(CANVAS_ITEMS) || handStack.is(PAINTER_PALETTE_ITEM) || handStack.isEmpty();
		if (this.fixed) {
			return InteractionResult.PASS;
		} else if (player instanceof ServerPlayer serverPlayer) {
			if (!this.isRemoved()) {
				this.doInteract(serverPlayer, handStack, hand);
			}

			return InteractionResult.CONSUME;
		} else {
			return this.getItem().isEmpty() && !handStackValid ? InteractionResult.PASS : InteractionResult.SUCCESS;
		}
	}

	private void doInteract(ServerPlayer player, ItemStack handStack, InteractionHand hand) {
		if (handStack.is(PAINTER_PALETTE_ITEM) && !this.getItem().isEmpty() && !this.getItem().is(WAXED_CANVAS_ITEMS)) {
			var payload = new CanvasOpenGuiPayload(this.getId(), this.getItem(), handStack);
			ServerPlayNetworking.send(player, payload);
		} else if ((handStack.is(CANVAS_ITEMS) || handStack.isEmpty()) && this.swapItem(player, handStack, hand)) {
			this.gameEvent(GameEvent.BLOCK_CHANGE, player);
		}
	}

	private boolean swapItem(ServerPlayer player, ItemStack stack, InteractionHand hand) {
		ItemStack currentStack = this.getItem();
		if (player.getAbilities().instabuild && currentStack.isEmpty() && !stack.isEmpty()) {
			this.setItem(stack.copyWithCount(1));
			PUT_CANVAS_ON_EASEL_TRIGGER.trigger(player, this, this.getItem());
			return true;
		} else if (stack.isEmpty() || stack.getCount() <= 1) {
			this.setItem(stack);
			player.setItemInHand(hand, currentStack);

			if (!this.getItem().isEmpty()) {
				PUT_CANVAS_ON_EASEL_TRIGGER.trigger(player, this, this.getItem());
			}

			return true;
		} else if (!currentStack.isEmpty()) {
			return false;
		} else {
			this.setItem(stack.split(1));
			if (!this.getItem().isEmpty()) {
				PUT_CANVAS_ON_EASEL_TRIGGER.trigger(player, this, this.getItem());
			}
			return true;
		}
	}

	@Override
	public ItemStack getPickResult() {
		ItemStack stack = this.getItem();
		return stack.isEmpty() ? new ItemStack(AurorasCanvasRegistry.EASEL_ITEM) : stack.copy();
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return this.fixed ? PushReaction.IGNORE : super.getPistonPushReaction();
	}

	/* Breaking */

	@Override
	public void kill() {
		this.remove(Entity.RemovalReason.KILLED);
		this.gameEvent(GameEvent.ENTITY_DIE);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.isRemoved()) {
			return false;
		} else if (this.level() instanceof ServerLevel level) {
			if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
				this.kill();
				return false;
			} else if (this.isInvulnerableTo(source) || this.fixed) {
				return false;
			} else if (source.is(DamageTypeTags.IS_EXPLOSION)) {
				this.brokenByAnything(level, source);
				this.kill();
				return false;
			} else if (source.is(AurorasCanvasRegistry.IGNITES_EASELS)) {
				if (this.isOnFire()) {
					this.causeDamage(level, source, 0.15f);
				} else {
					this.igniteForSeconds(5);
				}

				return false;
			} else if (source.is(AurorasCanvasRegistry.BURNS_EASELS) && this.getHealth() > 0.5f) {
				this.causeDamage(level, source, 4.f);
				return false;
			} else {
				boolean bl = source.getDirectEntity() instanceof AbstractArrow;
				boolean bl2 = bl && ((AbstractArrow) source.getDirectEntity()).getPierceLevel() > 0;
				boolean bl3 = "player".equals(source.getMsgId());
				if (!bl3 && !bl) {
					return false;
				} else if (source.getEntity() instanceof Player player && !player.getAbilities().mayBuild) {
					return false;
				} else if (source.isCreativePlayer()) {
					this.playBrokenSound();
					this.showBreakingParticles();
					this.kill();
					return bl2;
				} else {
					long currentTime = this.level().getGameTime();
					if (currentTime - this.lastHit > 5L && !bl) {
						this.level().broadcastEntityEvent(this, (byte) 32);
						this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
						this.lastHit = currentTime;
					} else {
						this.brokenByPlayer(level, source);
						this.showBreakingParticles();
						this.kill();
					}

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean skipAttackInteraction(Entity entity) {
		return entity instanceof Player player && !this.level().mayInteract(player, this.blockPosition());
	}

	@Override
	public boolean attackable() {
		return false;
	}

	@Override
	public boolean ignoreExplosion(Explosion explosion) {
		return this.fixed;
	}

	private void showBreakingParticles() {
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(
					new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()),
					this.getX(),
					this.getY(0.6666666666666666),
					this.getZ(),
					10,
					this.getBbWidth() / 4.f,
					this.getBbHeight() / 4.f,
					this.getBbWidth() / 4.f,
					0.05
			);
		}
	}

	private void causeDamage(ServerLevel level, DamageSource damageSource, float amount) {
		float health = this.getHealth();
		health -= amount;
		if (health <= 0.5f) {
			this.brokenByAnything(level, damageSource);
			this.kill();
		} else {
			this.setHealth(health);
			this.gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
		}
	}

	private void brokenByPlayer(ServerLevel level, DamageSource damageSource) {
		ItemStack stack = new ItemStack(AurorasCanvasRegistry.EASEL_ITEM);
		if (this.hasCustomName()) {
			stack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
		}

		Block.popResource(this.level(), this.blockPosition(), stack);
		this.brokenByAnything(level, damageSource);
	}

	private void brokenByAnything(ServerLevel level, DamageSource damageSource) {
		this.playBrokenSound();
		this.dropAllDeathLoot(level, damageSource);

		ItemStack stack = this.getItem();
		if (!stack.isEmpty()) {
			Block.popResource(this.level(), this.blockPosition().above(), stack);
			this.setItem(stack, false);
		}
	}

	private void playBrokenSound() {
		this.level().playSound(
				null,
				this.getX(), this.getY(), this.getZ(),
				AurorasCanvasSoundEvents.EASEL_BREAK, this.getSoundSource(),
				1.f, 1.f
		);
	}

	@Override
	public void thunderHit(ServerLevel level, LightningBolt lightning) {}

	/* AI */

	@Override
	public boolean isEffectiveAi() {
		return super.isEffectiveAi() && this.hasPhysics();
	}

	@Override
	public boolean canBeSeenByAnyone() {
		return !this.isInvisible();
	}

	/* Networking */

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 32) {
			if (this.level().isClientSide()) {
				this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), AurorasCanvasSoundEvents.EASEL_HIT, this.getSoundSource(), 0.3f, 1.f, false);
				this.lastHit = this.level().getGameTime();
			}
		} else {
			super.handleEntityEvent(id);
		}
	}

	/* Living Entity stuff */

	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return List.of();
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slot) {
		return (slot == EquipmentSlot.MAINHAND) ? this.getItem() : ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
		if (slot == EquipmentSlot.MAINHAND) {
			this.setItem(stack, true);
		}
	}

	@Override
	public HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}

	@Override
	public boolean canTakeItem(ItemStack stack) {
		EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(stack);
		return this.getItemBySlot(equipmentSlot).isEmpty() && stack.is(CANVAS_ITEMS);
	}

	/* Sounds */

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return AurorasCanvasSoundEvents.EASEL_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return AurorasCanvasSoundEvents.EASEL_BREAK;
	}

	@Override
	public LivingEntity.Fallsounds getFallSounds() {
		return new LivingEntity.Fallsounds(AurorasCanvasSoundEvents.EASEL_FALL, AurorasCanvasSoundEvents.EASEL_FALL);
	}
}
