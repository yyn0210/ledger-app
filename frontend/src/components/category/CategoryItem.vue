<template>
  <div class="category-item" :style="{ borderLeftColor: category.color }">
    <n-space align="center" justify="space-between">
      <n-space align="center">
        <div
          class="category-icon"
          :style="{ backgroundColor: category.color || '#18a058' }"
        >
          <n-icon :component="getIconComponent(category.icon)" size="18" color="#fff" />
        </div>

        <div>
          <n-text strong>{{ category.name }}</n-text>
          <n-text v-if="category.transactionCount" depth="3" style="font-size: 12px">
            · {{ category.transactionCount }} 笔
          </n-text>
        </div>
      </n-space>

      <n-space v-if="showActions" align="center">
        <n-button size="small" quaternary @click="$emit('edit', category)">
          <template #icon>
            <n-icon :component="CreateOutline" />
          </template>
        </n-button>
        <n-button
          size="small"
          quaternary
          type="error"
          @click="$emit('delete', category)"
        >
          <template #icon>
            <n-icon :component="TrashOutline" />
          </template>
        </n-button>
      </n-space>
    </n-space>
  </div>
</template>

<script setup>
import { CreateOutline, TrashOutline } from '@vicons/ionicons5'
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

defineProps({
  category: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

defineEmits(['edit', 'delete'])

// 图标映射
const iconMap = {
  restaurant: RestaurantOutline,
  coffee: CoffeeOutline,
  beer: BeerOutline,
  icecream: IceCreamOutline,
  car: CarOutline,
  bus: BusOutline,
  airplane: AirplaneOutline,
  bicycle: BicycleOutline,
  cart: CartOutline,
  bag: BagOutline,
  gift: GiftOutline,
  diamond: DiamondOutline,
  film: FilmOutline,
  game: GameControllerOutline,
  music: MusicalNotesOutline,
  basketball: BasketballOutline,
  wallet: WalletOutline,
  briefcase: BriefcaseOutline,
  trending: TrendingUpOutline,
  card: CardOutline,
  home: HomeOutline,
  phone: PhonePortraitOutline,
  medical: MedicalOutline,
  book: BookOutline,
  pet: PawOutline,
  sparkles: SparklesOutline
}

const getIconComponent = (iconName) => {
  return iconMap[iconName] || WalletOutline
}
</script>

<style scoped>
.category-item {
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border-left: 4px solid;
  transition: box-shadow 0.2s;
}

.category-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.category-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>
