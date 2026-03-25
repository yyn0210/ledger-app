<template>
  <div class="icon-picker">
    <n-popover trigger="click" placement="bottom-start" :width="320">
      <template #trigger>
        <n-button :style="{ backgroundColor: selectedIcon?.color || '#18a058' }" text>
          <template #icon>
            <n-icon :component="selectedIcon?.component" size="20" color="#fff" />
          </template>
          {{ selectedIcon?.name || '选择图标' }}
        </n-button>
      </template>

      <div>
        <n-tabs type="line" animated>
          <n-tab-pane name="food" tab="🍔 餐饮">
            <n-grid :cols="6" :x-gap="8" :y-gap="8">
              <n-grid-item
                v-for="icon in icons.food"
                :key="icon.name"
                @click="handleSelect(icon)"
              >
                <div
                  class="icon-item"
                  :class="{ selected: selectedIcon?.name === icon.name }"
                  :style="{ backgroundColor: icon.color }"
                >
                  <n-icon :component="icon.component" size="20" color="#fff" />
                </div>
              </n-grid-item>
            </n-grid>
          </n-tab-pane>

          <n-tab-pane name="transport" tab="🚗 交通">
            <n-grid :cols="6" :x-gap="8" :y-gap="8">
              <n-grid-item
                v-for="icon in icons.transport"
                :key="icon.name"
                @click="handleSelect(icon)"
              >
                <div
                  class="icon-item"
                  :class="{ selected: selectedIcon?.name === icon.name }"
                  :style="{ backgroundColor: icon.color }"
                >
                  <n-icon :component="icon.component" size="20" color="#fff" />
                </div>
              </n-grid-item>
            </n-grid>
          </n-tab-pane>

          <n-tab-pane name="shopping" tab="🛒 购物">
            <n-grid :cols="6" :x-gap="8" :y-gap="8">
              <n-grid-item
                v-for="icon in icons.shopping"
                :key="icon.name"
                @click="handleSelect(icon)"
              >
                <div
                  class="icon-item"
                  :class="{ selected: selectedIcon?.name === icon.name }"
                  :style="{ backgroundColor: icon.color }"
                >
                  <n-icon :component="icon.component" size="20" color="#fff" />
                </div>
              </n-grid-item>
            </n-grid>
          </n-tab-pane>

          <n-tab-pane name="entertainment" tab="🎬 娱乐">
            <n-grid :cols="6" :x-gap="8" :y-gap="8">
              <n-grid-item
                v-for="icon in icons.entertainment"
                :key="icon.name"
                @click="handleSelect(icon)"
              >
                <div
                  class="icon-item"
                  :class="{ selected: selectedIcon?.name === icon.name }"
                  :style="{ backgroundColor: icon.color }"
                >
                  <n-icon :component="icon.component" size="20" color="#fff" />
                </div>
              </n-grid-item>
            </n-grid>
          </n-tab-pane>

          <n-tab-pane name="income" tab="💰 收入">
            <n-grid :cols="6" :x-gap="8" :y-gap="8">
              <n-grid-item
                v-for="icon in icons.income"
                :key="icon.name"
                @click="handleSelect(icon)"
              >
                <div
                  class="icon-item"
                  :class="{ selected: selectedIcon?.name === icon.name }"
                  :style="{ backgroundColor: icon.color }"
                >
                  <n-icon :component="icon.component" size="20" color="#fff" />
                </div>
              </n-grid-item>
            </n-grid>
          </n-tab-pane>

          <n-tab-pane name="other" tab="📦 其他">
            <n-grid :cols="6" :x-gap="8" :y-gap="8">
              <n-grid-item
                v-for="icon in icons.other"
                :key="icon.name"
                @click="handleSelect(icon)"
              >
                <div
                  class="icon-item"
                  :class="{ selected: selectedIcon?.name === icon.name }"
                  :style="{ backgroundColor: icon.color }"
                >
                  <n-icon :component="icon.component" size="20" color="#fff" />
                </div>
              </n-grid-item>
            </n-grid>
          </n-tab-pane>
        </n-tabs>
      </div>
    </n-popover>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  RestaurantOutline,
  CoffeeOutline,
  BeerOutline,
  IceCreamOutline,
  CarOutline,
  BusOutline,
  AirplaneOutline,
  BicycleOutline,
  CartOutline,
  BagOutline,
  GiftOutline,
  DiamondOutline,
  FilmOutline,
  GameControllerOutline,
  MusicalNotesOutline,
  BasketballOutline,
  WalletOutline,
  BriefcaseOutline,
  TrendingUpOutline,
  CardOutline,
  HomeOutline,
  PhonePortraitOutline,
  MedicalOutline,
  BookOutline,
  PawOutline,
  SparklesOutline
} from '@vicons/ionicons5'

const props = defineProps({
  modelValue: String,
  modelColor: String
})

const emit = defineEmits(['update:modelValue', 'update:modelColor'])

const selectedIcon = computed(() => {
  if (!props.modelValue) return null
  const allIcons = Object.values(icons).flat()
  return allIcons.find(i => i.name === props.modelValue)
})

// 图标库
const icons = {
  food: [
    { name: 'restaurant', component: RestaurantOutline, color: '#ff6b6b' },
    { name: 'coffee', component: CoffeeOutline, color: '#8b4513' },
    { name: 'beer', component: BeerOutline, color: '#f59e0b' },
    { name: 'icecream', component: IceCreamOutline, color: '#ec4899' }
  ],
  transport: [
    { name: 'car', component: CarOutline, color: '#3b82f6' },
    { name: 'bus', component: BusOutline, color: '#10b981' },
    { name: 'airplane', component: AirplaneOutline, color: '#06b6d4' },
    { name: 'bicycle', component: BicycleOutline, color: '#84cc16' }
  ],
  shopping: [
    { name: 'cart', component: CartOutline, color: '#f97316' },
    { name: 'bag', component: BagOutline, color: '#8b5cf6' },
    { name: 'gift', component: GiftOutline, color: '#ef4444' },
    { name: 'diamond', component: DiamondOutline, color: '#06b6d4' }
  ],
  entertainment: [
    { name: 'film', component: FilmOutline, color: '#dc2626' },
    { name: 'game', component: GameControllerOutline, color: '#7c3aed' },
    { name: 'music', component: MusicalNotesOutline, color: '#ec4899' },
    { name: 'basketball', component: BasketballOutline, color: '#f97316' }
  ],
  income: [
    { name: 'wallet', component: WalletOutline, color: '#22c55e' },
    { name: 'briefcase', component: BriefcaseOutline, color: '#1e40af' },
    { name: 'trending', component: TrendingUpOutline, color: '#10b981' },
    { name: 'card', component: CardOutline, color: '#6366f1' }
  ],
  other: [
    { name: 'home', component: HomeOutline, color: '#64748b' },
    { name: 'phone', component: PhonePortraitOutline, color: '#3b82f6' },
    { name: 'medical', component: MedicalOutline, color: '#ef4444' },
    { name: 'book', component: BookOutline, color: '#8b5cf6' },
    { name: 'pet', component: PawOutline, color: '#f59e0b' },
    { name: 'sparkles', component: SparklesOutline, color: '#eab308' }
  ]
}

const handleSelect = (icon) => {
  emit('update:modelValue', icon.name)
  emit('update:modelColor', icon.color)
}
</script>

<style scoped>
.icon-picker {
  display: inline-block;
}

.icon-item {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.icon-item:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.icon-item.selected {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.5);
}
</style>
