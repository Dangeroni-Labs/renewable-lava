package dev.dangeroni.renewablelava.client;

import dev.dangeroni.renewablelava.config.RenewableLavaConfig;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class RenewableLavaConfigScreen extends Screen {
	private static final int CONTENT_WIDTH = 310;
	private static final int CONTROL_WIDTH = 310;
	private static final int CONTROL_HEIGHT = 20;

	private final Screen parent;

	private CycleButton<Boolean> enabledButton;
	private CycleButton<Integer> requiredNeighboursButton;
	private EditBox whitelistDimensionsBox;
	private Button doneButton;
	private boolean enabledValue;
	private int requiredNeighboursValue;
	private @Nullable String invalidDimensionId;
	private boolean saveFailed;

	public RenewableLavaConfigScreen(Screen parent) {
		super(Component.translatable("renewable_lava.config.title"));
		this.parent = parent;

		RenewableLavaConfig config = RenewableLavaConfig.get();
		this.enabledValue = config.enabled();
		this.requiredNeighboursValue = config.requiredSourceNeighbours();
		this.invalidDimensionId = null;
		this.saveFailed = false;
	}

	@Override
	protected void init() {
		int left = this.width / 2 - CONTENT_WIDTH / 2;
		int controlX = left;
		boolean editable = this.canEditConfig();
		Tooltip serverOnlyTooltip = editable ? null : Tooltip.create(Component.translatable("renewable_lava.config.server_only"));

		this.enabledButton = this.addRenderableWidget(
			CycleButton.onOffBuilder(this.enabledValue)
				.withTooltip(value -> Tooltip.create(Component.translatable("renewable_lava.config.enabled.tooltip")))
				.create(controlX, 56, CONTROL_WIDTH, CONTROL_HEIGHT, Component.translatable("renewable_lava.config.enabled"), (button, value) -> {
					this.enabledValue = value;
				})
		);
		this.enabledButton.active = editable;
		if (!editable) {
			this.enabledButton.setTooltip(serverOnlyTooltip);
		}

		this.requiredNeighboursButton = this.addRenderableWidget(
			CycleButton.builder(value -> Component.literal(Integer.toString(value)), RenewableLavaConfig.DEFAULT_REQUIRED_SOURCE_NEIGHBOURS)
				.withValues(List.of(2, 3, 4))
				.withTooltip(value -> Tooltip.create(Component.translatable("renewable_lava.config.required_source_neighbours.tooltip")))
				.create(controlX, 106, CONTROL_WIDTH, CONTROL_HEIGHT, Component.translatable("renewable_lava.config.required_source_neighbours"), (button, value) -> {
					this.requiredNeighboursValue = value;
				})
		);
		this.requiredNeighboursButton.setValue(this.requiredNeighboursValue);
		this.requiredNeighboursButton.active = editable;
		if (!editable) {
			this.requiredNeighboursButton.setTooltip(serverOnlyTooltip);
		}

		this.whitelistDimensionsBox = this.addRenderableWidget(
			new EditBox(this.font, controlX, 156, CONTROL_WIDTH, CONTROL_HEIGHT, Component.translatable("renewable_lava.config.whitelist_dimensions"))
		);
		this.whitelistDimensionsBox.setMaxLength(512);
		this.whitelistDimensionsBox.setHint(Component.literal(String.join(", ", RenewableLavaConfig.DEFAULT_WHITELIST_DIMENSIONS)));
		this.whitelistDimensionsBox.setTooltip(Tooltip.create(Component.translatable("renewable_lava.config.whitelist_dimensions.tooltip")));
		this.whitelistDimensionsBox.setValue(String.join(", ", RenewableLavaConfig.get().whitelistDimensionIds()));
		this.whitelistDimensionsBox.setEditable(editable);
		this.whitelistDimensionsBox.setResponder(value -> this.onWhitelistDimensionsChanged());

		this.doneButton = this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.saveAndClose()).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build()
		);
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build()
		);

		this.onWhitelistDimensionsChanged();
		this.updateControlStates(editable);
		this.setInitialFocus(this.whitelistDimensionsBox);
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.parent);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);

		int left = this.width / 2 - CONTENT_WIDTH / 2;
		graphics.centeredText(this.font, this.title, this.width / 2, 20, 16777215);
		graphics.text(this.font, Component.translatable("renewable_lava.config.enabled"), left, 44, 16777215, false);
		graphics.text(this.font, Component.translatable("renewable_lava.config.required_source_neighbours"), left, 94, 16777215, false);
		graphics.text(this.font, Component.translatable("renewable_lava.config.whitelist_dimensions"), left, 144, 16777215, false);

		if (!this.canEditConfig()) {
			graphics.textWithWordWrap(
				this.font,
				Component.translatable("renewable_lava.config.server_only"),
				left,
				184,
				CONTENT_WIDTH,
				11184810,
				false
			);
		} else if (this.invalidDimensionId != null) {
			graphics.text(
				this.font,
				Component.translatable("renewable_lava.config.invalid_dimension", this.invalidDimensionId),
				left,
				184,
				16733525,
				false
			);
		} else if (this.saveFailed) {
			graphics.text(this.font, Component.translatable("renewable_lava.config.save_failed"), left, 184, 16733525, false);
		}
	}

	private boolean canEditConfig() {
		return this.minecraft.level == null || this.minecraft.isLocalServer();
	}

	private void onWhitelistDimensionsChanged() {
		this.invalidDimensionId = RenewableLavaConfig
			.findInvalidWhitelistDimension(RenewableLavaConfig.parseWhitelistDimensions(this.whitelistDimensionsBox.getValue()))
			.orElse(null);
		this.saveFailed = false;
		this.updateControlStates(this.canEditConfig());
	}

	private void updateControlStates(boolean editable) {
		if (this.doneButton != null) {
			this.doneButton.active = editable && this.invalidDimensionId == null;
		}
	}

	private void saveAndClose() {
		RenewableLavaConfig.SaveResult result = RenewableLavaConfig.save(
			this.enabledValue,
			this.requiredNeighboursValue,
			RenewableLavaConfig.parseWhitelistDimensions(this.whitelistDimensionsBox.getValue())
		);
		if (result.success()) {
			this.minecraft.gui.setScreen(this.parent);
			return;
		}

		this.invalidDimensionId = result.invalidDimensionId();
		this.saveFailed = this.invalidDimensionId == null;
		this.updateControlStates(this.canEditConfig());
	}
}
