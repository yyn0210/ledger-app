"""
智能推荐服务
基于历史交易记录学习用户分类习惯
"""
import re
from typing import List, Dict, Optional
from loguru import logger

from app.utils.redis import redis_client


class RecommenderService:
    """智能推荐服务"""
    
    def __init__(self):
        self.merchant_patterns = self._init_merchant_patterns()
    
    def _init_merchant_patterns(self) -> Dict[str, List[str]]:
        """初始化商户关键词模式"""
        return {
            "餐饮": ["餐厅", "饭店", "小吃", "快餐", "咖啡", "奶茶", "面包", "美食", "外卖", "饿了么", "美团"],
            "交通": ["打车", "滴滴", " Uber", "地铁", "公交", "火车", "飞机", "加油", "停车", "共享单车"],
            "购物": ["超市", "商场", "淘宝", "京东", "拼多多", "天猫", "便利店", "百货"],
            "娱乐": ["电影", "KTV", "游戏", "演出", "展览", "博物馆", "游乐园", "健身"],
            "生活": ["水电", "燃气", "物业", "房租", "搬家", "维修", "理发", "洗衣"],
            "医疗": ["医院", "药店", "诊所", "体检", "药品", "医疗"],
            "教育": ["培训", "课程", "书籍", "学费", "考试", "学校"],
            "数码": ["手机", "电脑", "电子", "苹果", "华为", "小米", "数码"],
            "旅游": ["酒店", "机票", "旅行", "景点", "民宿", "签证"],
            "人情": ["红包", "礼物", "请客", "聚会", "婚礼", "生日"]
        }
    
    def recommend_by_merchant(self, merchant: str, history: List[Dict] = None) -> List[Dict]:
        """
        根据商户名推荐分类
        
        Args:
            merchant: 商户名称
            history: 历史交易记录
            
        Returns:
            推荐分类列表
        """
        recommendations = []
        
        # 1. 基于关键词匹配
        keyword_matches = self._match_by_keywords(merchant)
        recommendations.extend(keyword_matches)
        
        # 2. 基于历史交易学习
        if history:
            history_matches = self._match_by_history(merchant, history)
            recommendations.extend(history_matches)
        
        # 去重并排序
        seen = set()
        unique_recs = []
        for rec in recommendations:
            if rec["category_name"] not in seen:
                seen.add(rec["category_name"])
                unique_recs.append(rec)
        
        # 按置信度排序
        unique_recs.sort(key=lambda x: x["confidence"], reverse=True)
        
        return unique_recs
    
    def _match_by_keywords(self, merchant: str) -> List[Dict]:
        """基于关键词匹配"""
        matches = []
        merchant_lower = merchant.lower()
        
        for category, keywords in self.merchant_patterns.items():
            matched_keywords = [kw for kw in keywords if kw.lower() in merchant_lower]
            
            if matched_keywords:
                confidence = min(0.5 + len(matched_keywords) * 0.15, 0.95)
                matches.append({
                    "category_name": category,
                    "confidence": round(confidence, 2),
                    "reason": f"商户名包含：{', '.join(matched_keywords)}",
                    "match_count": len(matched_keywords)
                })
        
        return matches
    
    def _match_by_history(self, merchant: str, history: List[Dict]) -> List[Dict]:
        """基于历史交易匹配"""
        # 统计该商户的历史分类
        merchant_history = {}
        
        for tx in history:
            tx_merchant = tx.get("merchant", "")
            category = tx.get("category")
            
            if not category:
                continue
            
            # 模糊匹配商户名
            if self._similar(merchant, tx_merchant):
                if category not in merchant_history:
                    merchant_history[category] = 0
                merchant_history[category] += 1
        
        # 转换为推荐格式
        matches = []
        total = sum(merchant_history.values())
        
        for category, count in merchant_history.items():
            confidence = count / total if total > 0 else 0
            matches.append({
                "category_name": category,
                "confidence": round(min(confidence + 0.3, 0.95), 2),  # 历史匹配置信度更高
                "reason": f"历史交易中有 {count} 笔相似商户归为此类",
                "match_count": count
            })
        
        return matches
    
    def _similar(self, text1: str, text2: str, threshold: float = 0.6) -> bool:
        """简单的字符串相似度判断"""
        if not text1 or not text2:
            return False
        
        # 完全匹配
        if text1 == text2:
            return True
        
        # 包含关系
        if text1 in text2 or text2 in text1:
            return True
        
        # 简单 Jaccard 相似度
        set1 = set(text1.lower())
        set2 = set(text2.lower())
        
        intersection = len(set1 & set2)
        union = len(set1 | set2)
        
        similarity = intersection / union if union > 0 else 0
        
        return similarity >= threshold
    
    def get_recommendations(
        self,
        merchant: Optional[str] = None,
        amount: Optional[float] = None,
        note: Optional[str] = None,
        history: List[Dict] = None
    ) -> List[Dict]:
        """
        综合推荐
        
        Args:
            merchant: 商户名称
            amount: 金额
            note: 备注
            history: 历史交易记录
            
        Returns:
            推荐分类列表
        """
        all_recommendations = []
        
        # 商户名推荐
        if merchant:
            merchant_recs = self.recommend_by_merchant(merchant, history)
            all_recommendations.extend(merchant_recs)
        
        # 备注推荐
        if note and not all_recommendations:
            note_recs = self.recommend_by_merchant(note, history)
            all_recommendations.extend(note_recs)
        
        # 去重排序
        seen = set()
        unique_recs = []
        for rec in all_recommendations:
            if rec["category_name"] not in seen:
                seen.add(rec["category_name"])
                unique_recs.append(rec)
        
        unique_recs.sort(key=lambda x: x["confidence"], reverse=True)
        
        # 如果没有推荐，返回默认分类
        if not unique_recs:
            unique_recs.append({
                "category_name": "其他",
                "confidence": 0.5,
                "reason": "未找到匹配的分类，建议使用默认分类",
                "match_count": 0
            })
        
        return unique_recs


# 全局推荐服务实例
recommender_service = RecommenderService()
