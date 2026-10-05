package aeind.mixin;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class AeindMixinConfigPlugin implements IMixinConfigPlugin {
   private static final String JADE_MIXIN = "aeind.mixin.MachineComponentFluidsProviderMixin";
   private static final String TESSERACT_MIXIN_PREFIX = "aeind.mixin.Tesseract";
   private static final String TESSERACT_ACCESSOR = "aeind.mixin.MultipliedCrafterComponentAccessor";
   private static final String TESSERACT_MODULAR_ACCESSOR = "aeind.mixin.AbstractModularCrafterComponentAccessor";

   @Override
   public void onLoad(String mixinPackage) {
   }

   @Override
   public String getRefMapperConfig() {
      return null;
   }

   @Override
   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      if (JADE_MIXIN.equals(mixinClassName)) {
         return LoadingModList.get().getModFileById("jade") != null;
      }
      return (!mixinClassName.startsWith(TESSERACT_MIXIN_PREFIX)
         && !TESSERACT_ACCESSOR.equals(mixinClassName)
         && !TESSERACT_MODULAR_ACCESSOR.equals(mixinClassName))
         || LoadingModList.get().getModFileById("tesseract_api") != null;
   }

   @Override
   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   @Override
   public List<String> getMixins() {
      return null;
   }

   @Override
   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   @Override
   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
