# Demo 截图目录

此目录用于放置产品真实运行截图。请在录制同一轮 Demo 时截取，不使用设计稿或伪造结果。

三张核心截图已于 2026-08-07 通过本地完整流程生成。

## 必需截图

| 文件名 | 内容 | 检查重点 |
| --- | --- | --- |
| `01-home.png` | 首页 | Logo、产品定位、商品和市场输入完整可见 |
| `02-agent-workflow.png` | Agent 执行页 | 已完成、执行中、等待三类状态尽量同时出现 |
| `03-analysis-report.png` | 报告页 | 机会评分、趋势、竞争程度与策略内容可见 |

可选增加 `04-pdf-export.png`，展示导出后的 PDF 或打印预览。

## 建议规格

- 推荐 1440 × 900，最低宽度 1280 px。
- 浏览器缩放 100%，不截取地址栏、书签栏和操作系统通知。
- 使用仓库默认示例 `Portable Blender / USA Amazon`。
- 截图前确认没有个人信息、API Key 或调试浮层。
- PNG 格式，单张尽量控制在 1 MB 内，便于 GitHub 加载。

截图完成后，可在项目根 README 的 Demo 区域加入：

```markdown
![CrossMind AI 首页](docs/screenshots/01-home.png)
![多 Agent 分析过程](docs/screenshots/02-agent-workflow.png)
![市场进入分析报告](docs/screenshots/03-analysis-report.png)
```
