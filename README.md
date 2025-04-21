
---

# FinChat: 智能理财助手

FinChat 是一款基于中国大语言模型的智能理财分析工具，旨在为用户提供个性化的理财建议和投资策略。

## 项目概述

随着金融市场的复杂性增加，投资者需要更智能的工具来辅助决策。FinChat 利用先进的自然语言处理技术，分析市场数据，为用户提供实时的理财建议。

## 主要功能

- **实时市场分析**：获取最新的市场动态和趋势。
- **个性化投资建议**：根据用户的风险偏好和投资目标，提供定制化的投资组合建议。
- **财务健康评估**：分析用户的财务状况，提出优化建议。
- **智能问答系统**：用户可以通过自然语言提问，系统提供智能回答。

## 技术栈

- **编程语言**：Java
- **构建工具**：Maven
- **前端技术**：CSS
- **自然语言处理**：集成中国大语言模型

## 安装与运行

1. 克隆本仓库：

   ```bash
   git clone https://github.com/yoho520/Group73.git
   ```


2. 进入项目目录：

   ```bash
   cd Group73
   ```


3. 使用 Maven 构建项目：

   ```bash
   mvn clean install
   ```


4. 运行应用程序：

   ```bash
   mvn exec:java
   ```


## 项目结构


```plaintext
Group73/
├── LICENSE
├── README.md
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── finchat/
    │   │           ├── App.java
    │   │           └── services/
    │   │               ├── MarketAnalysisService.java
    │   │               └── InvestmentAdviceService.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
            └── com/
                └── finchat/
                    └── AppTest.java
```

## 许可证

本项目采用 MIT 许可证，详细信息请参阅 [LICENSE](LICENSE) 文件。

---
