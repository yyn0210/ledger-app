<template>
	<view class="stats-page">
		<!-- 顶部月份选择 -->
		<view class="header">
			<u-icon name="arrow-left" size="20" color="#fff" @click="prevMonth"></u-icon>
			<text class="month-text">{{ currentYear }}年{{ currentMonth }}月</text>
			<u-icon name="arrow-right" size="20" color="#fff" @click="nextMonth"></u-icon>
		</view>

		<!-- 月度概览 -->
		<view class="overview-card">
			<view class="overview-item">
				<view class="overview-icon income">
					<u-icon name="arrow-up-circle" size="24" color="#10B981"></u-icon>
				</view>
				<view class="overview-info">
					<text class="overview-label">本月收入</text>
					<text class="overview-value income">¥{{ monthlyStats.income }}</text>
				</view>
			</view>
			<view class="overview-divider"></view>
			<view class="overview-item">
				<view class="overview-icon expense">
					<u-icon name="arrow-down-circle" size="24" color="#EF4444"></u-icon>
				</view>
				<view class="overview-info">
					<text class="overview-label">本月支出</text>
					<text class="overview-value expense">¥{{ monthlyStats.expense }}</text>
				</view>
			</view>
			<view class="overview-divider"></view>
			<view class="overview-item">
				<view class="overview-icon balance">
					<u-icon name="wallet" size="24" color="#3B82F6"></u-icon>
				</view>
				<view class="overview-info">
					<text class="overview-label">本月结余</text>
					<text class="overview-value balance">¥{{ monthlyStats.balance }}</text>
				</view>
			</view>
		</view>

		<!-- 收支趋势图 -->
		<view class="chart-section">
			<view class="section-header">
				<text class="section-title">收支趋势</text>
				<view class="chart-type-switch">
					<text
						:class="['switch-item', chartType === 'day' ? 'active' : '']"
						@click="chartType = 'day'"
					>日</text>
					<text
						:class="['switch-item', chartType === 'week' ? 'active' : '']"
						@click="chartType = 'week'"
					>周</text>
				</view>
			</view>
			<view class="chart-container">
				<mp-echarts
					ref="trendChart"
					:option="trendOption"
					:canvasId="trendCanvasId"
					:id="trendCanvasId"
					class="echart-chart"
				></mp-echarts>
			</view>
		</view>

		<!-- 分类占比图 -->
		<view class="chart-section">
			<view class="section-header">
				<text class="section-title">支出分类</text>
				<text class="section-total">总支出 ¥{{ monthlyStats.expense }}</text>
			</view>
			<view class="chart-container">
				<mp-echarts
					ref="pieChart"
					:option="pieOption"
					:canvasId="pieCanvasId"
					:id="pieCanvasId"
					class="echart-chart"
				></mp-echarts>
			</view>
			<!-- 分类图例 -->
			<view class="category-legend">
				<view
					v-for="(item, index) in categoryLegend"
					:key="index"
					class="legend-item"
				>
					<view class="legend-color" :style="{ backgroundColor: item.color }"></view>
					<text class="legend-name">{{ item.name }}</text>
					<text class="legend-value">¥{{ item.value }}</text>
					<text class="legend-percent">{{ item.percent }}%</text>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<u-empty
			v-if="noData"
			mode="data"
			text="本月暂无数据"
			style="margin-top: 40px"
		></u-empty>
	</view>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import * as echarts from 'echarts';

export default {
	data() {
		return {
			currentDate: new Date(),
			chartType: 'day',
			trendCanvasId: 'trendChart',
			pieCanvasId: 'pieChart',
			noData: false,
			monthlyStats: {
				income: '0.00',
				expense: '0.00',
				balance: '0.00'
			},
			categoryLegend: [],
			trendOption: {},
			pieOption: {}
		};
	},
	computed: {
		currentMonth() {
			return String(this.currentDate.getMonth() + 1).padStart(2, '0');
		},
		currentYear() {
			return this.currentDate.getFullYear();
		}
	},
	onMounted() {
		this.loadData();
	},
	methods: {
		loadData() {
			// Mock 数据
			this.monthlyStats = {
				income: '15000.00',
				expense: '6580.00',
				balance: '8420.00'
			};

			// 分类数据
			const categoryData = [
				{ name: '餐饮', value: 2450, color: '#EF4444' },
				{ name: '交通', value: 680, color: '#3B82F6' },
				{ name: '购物', value: 1580, color: '#F59E0B' },
				{ name: '娱乐', value: 920, color: '#8B5CF6' },
				{ name: '医疗', value: 350, color: '#10B981' },
				{ name: '其他', value: 600, color: '#6B7280' }
			];

			const total = categoryData.reduce((sum, item) => sum + item.value, 0);
			this.categoryLegend = categoryData.map(item => ({
				...item,
				percent: ((item.value / total) * 100).toFixed(1)
			}));

			// 趋势图数据（按日）
			const daysInMonth = new Date(this.currentYear, this.currentDate.getMonth() + 1, 0).getDate();
			const dates = [];
			const incomeData = [];
			const expenseData = [];

			for (let i = 1; i <= daysInMonth; i++) {
				dates.push(`${i}日`);
				// Mock 随机数据
				incomeData.push(i === 15 ? 15000 : Math.random() > 0.7 ? Math.floor(Math.random() * 500) : 0);
				expenseData.push(Math.floor(Math.random() * 300));
			}

			// 趋势图配置
			this.trendOption = {
				tooltip: {
					trigger: 'axis'
				},
				legend: {
					data: ['收入', '支出'],
					bottom: 0,
					itemWidth: 12,
					itemHeight: 12,
					textStyle: {
						fontSize: 11
					}
				},
				grid: {
					left: '10%',
					right: '5%',
					top: '15%',
					bottom: '20%',
					containLabel: true
				},
				xAxis: {
					type: 'category',
					data: dates,
					boundaryGap: false,
					axisLabel: {
						fontSize: 10,
						rotate: 45,
						interval: Math.floor(dates.length / 15)
					},
					axisLine: {
						lineStyle: {
							color: '#E5E7EB'
						}
					}
				},
				yAxis: {
					type: 'value',
					axisLabel: {
						fontSize: 10,
						formatter: value => '¥' + value
					},
					splitLine: {
						lineStyle: {
							color: '#F3F4F6',
							type: 'dashed'
						}
					}
				},
				series: [
					{
						name: '收入',
						type: 'line',
						smooth: true,
						data: incomeData,
						itemStyle: {
							color: '#10B981'
						},
						areaStyle: {
							color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
								{ offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
								{ offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
							])
						}
					},
					{
						name: '支出',
						type: 'line',
						smooth: true,
						data: expenseData,
						itemStyle: {
							color: '#EF4444'
						},
						areaStyle: {
							color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
								{ offset: 0, color: 'rgba(239, 68, 68, 0.3)' },
								{ offset: 1, color: 'rgba(239, 68, 68, 0.05)' }
							])
						}
					}
				]
			};

			// 饼图配置
			this.pieOption = {
				tooltip: {
					trigger: 'item',
					formatter: '{b}: ¥{c} ({d}%)'
				},
				series: [
					{
						name: '支出分类',
						type: 'pie',
						radius: ['40%', '70%'],
						center: ['50%', '50%'],
						avoidLabelOverlap: false,
						itemStyle: {
							borderRadius: 6,
							borderColor: '#fff',
							borderWidth: 2
						},
						label: {
							show: false,
							position: 'center'
						},
						emphasis: {
							label: {
								show: true,
								fontSize: 14,
								fontWeight: 'bold'
							}
						},
						labelLine: {
							show: false
						},
						data: categoryData.map(item => ({
							name: item.name,
							value: item.value,
							itemStyle: {
								color: item.color
							}
						}))
					}
				]
			};
		},
		prevMonth() {
			this.currentDate.setMonth(this.currentDate.getMonth() - 1);
			this.loadData();
		},
		nextMonth() {
			this.currentDate.setMonth(this.currentDate.getMonth() + 1);
			this.loadData();
		}
	}
};
</script>

<style lang="scss" scoped>
.stats-page {
	min-height: 100vh;
	background: #F5F6F7;
}

.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 40px 20px 20px;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

	.month-text {
		font-size: 18px;
		font-weight: 600;
		color: #fff;
	}
}

.overview-card {
	display: flex;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	margin: 0 16px -60px;
	border-radius: 16px;
	padding: 20px 16px;

	.overview-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8px;

		.overview-icon {
			width: 44px;
			height: 44px;
			display: flex;
			align-items: center;
			justify-content: center;
			background: rgba(255, 255, 255, 0.2);
			border-radius: 12px;
		}

		.overview-info {
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 4px;

			.overview-label {
				font-size: 12px;
				color: rgba(255, 255, 255, 0.8);
			}

			.overview-value {
				font-size: 16px;
				font-weight: 600;
				color: #fff;

				&.income {
					color: #BBF7D0;
				}

				&.expense {
					color: #FECACA;
				}

				&.balance {
					color: #BFDBFE;
				}
			}
		}
	}

	.overview-divider {
		width: 1px;
		background: rgba(255, 255, 255, 0.3);
		margin: 0 8px;
	}
}

.chart-section {
	background: #fff;
	border-radius: 12px;
	margin: 16px;
	padding: 16px;

	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 16px;

		.section-title {
			font-size: 16px;
			font-weight: 600;
			color: #1F2937;
		}

		.section-total {
			font-size: 13px;
			color: #6B7280;
		}

		.chart-type-switch {
			display: flex;
			background: #F3F4F6;
			border-radius: 6px;
			padding: 2px;

			.switch-item {
				padding: 4px 12px;
				font-size: 12px;
				color: #6B7280;
				border-radius: 4px;

				&.active {
					background: #fff;
					color: #4F46E5;
					box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
				}
			}
		}
	}

	.chart-container {
		height: 220px;
		margin-bottom: 16px;

		.echart-chart {
			width: 100%;
			height: 100%;
		}
	}

	.category-legend {
		display: flex;
		flex-wrap: wrap;
		gap: 12px;

		.legend-item {
			display: flex;
			align-items: center;
			gap: 6px;
			width: calc(50% - 6px);

			.legend-color {
				width: 12px;
				height: 12px;
				border-radius: 3px;
				flex-shrink: 0;
			}

			.legend-name {
				flex: 1;
				font-size: 13px;
				color: #6B7280;
			}

			.legend-value {
				font-size: 13px;
				font-weight: 500;
				color: #1F2937;
			}

			.legend-percent {
				font-size: 12px;
				color: #9CA3AF;
				margin-left: 4px;
			}
		}
	}
}
</style>
