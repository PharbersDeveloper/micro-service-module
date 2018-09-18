package services.assess_report

trait dimension {

    val mapping: List[Map[String, String]] = List(
        Map(
            "eng_title" -> "report_analysi_val",
            "chin_title" -> "报表分析与决策"
        ),
        Map(
            "eng_title" -> "market_insight_val",
            "chin_title" -> "市场洞察力"
        ),
        Map(
            "eng_title" -> "target_setting_val",
            "chin_title" -> "目标分级能力"
        ),
        Map(
            "eng_title" -> "strategy_execution_val",
            "chin_title" -> "公司战略执行力"
        ),
        Map(
            "eng_title" -> "resource_allocation_val",
            "chin_title" -> "资源分配与优化"
        ),
        Map(
            "eng_title" -> "plan_deployment_val",
            "chin_title" -> "销售计划部署"
        ),
        Map(
            "eng_title" -> "leadership_val",
            "chin_title" -> "领导力"
        ),
        Map(
            "eng_title" -> "team_management_val",
            "chin_title" -> "管理能力"
        ),
        Map(
            "eng_title" -> "talent_train_val",
            "chin_title" -> "人才培养"
        )
    )

    def getScore(str: String)(score_results: List[Map[String, Any]], mapping: List[Map[String, String]]): String = {
        val chin_title = mapping.find(_("eng_title") == str).get("chin_title")
        score_results.find(_("item") == chin_title).map(_("score").asInstanceOf[String]).getOrElse("")
    }
}
