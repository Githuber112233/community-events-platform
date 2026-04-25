const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, BorderStyle, WidthType, ShadingType, VerticalAlign,
  PageOrientation, HeadingLevel
} = require('docx');
const fs = require('fs');

// ===== 颜色和边框定义 =====
const BORDER_SINGLE = { style: BorderStyle.SINGLE, size: 8, color: "000000" };
const BORDER_THIN   = { style: BorderStyle.SINGLE, size: 4, color: "000000" };
const BORDERS_ALL   = { top: BORDER_SINGLE, bottom: BORDER_SINGLE, left: BORDER_SINGLE, right: BORDER_SINGLE };
const BORDERS_THIN  = { top: BORDER_THIN, bottom: BORDER_THIN, left: BORDER_THIN, right: BORDER_THIN };
const CELL_MARGINS  = { top: 80, bottom: 80, left: 120, right: 120 };

// 页面设置：A4，页边距适中
const PAGE = {
  size: { width: 11906, height: 16838 },
  margin: { top: 1134, right: 1134, bottom: 1134, left: 1701 } // 上下2cm，左3cm，右2cm
};
const CONTENT_W = 11906 - 1134 - 1701; // = 9071 DXA

// ===== 辅助函数 =====
function cell(paragraphs, opts = {}) {
  return new TableCell({
    borders: BORDERS_THIN,
    margins: CELL_MARGINS,
    verticalAlign: VerticalAlign.CENTER,
    columnSpan: opts.colspan || 1,
    rowSpan: opts.rowspan || 1,
    width: opts.width ? { size: opts.width, type: WidthType.DXA } : undefined,
    shading: opts.shading ? { fill: opts.shading, type: ShadingType.CLEAR } : undefined,
    children: Array.isArray(paragraphs) ? paragraphs : [paragraphs],
  });
}

function para(text, opts = {}) {
  return new Paragraph({
    alignment: opts.align || AlignmentType.LEFT,
    spacing: { before: 40, after: 40 },
    children: [
      new TextRun({
        text: text,
        font: "宋体",
        size: opts.size || 22,   // 11pt = 22 half-pts
        bold: opts.bold || false,
        color: opts.color || "000000",
      })
    ]
  });
}

function labelPara(text) {
  return para(text, { bold: true, size: 22 });
}

function headerCell(text, w) {
  return cell(para(text, { bold: true, align: AlignmentType.CENTER, size: 22 }), {
    shading: "D9D9D9", width: w
  });
}

function emptyLine(size) {
  return new Paragraph({
    spacing: { before: 0, after: 0 },
    children: [new TextRun({ text: "", size: size || 22 })]
  });
}

// ===== 文档标题 =====
const titlePara = new Paragraph({
  alignment: AlignmentType.CENTER,
  spacing: { before: 200, after: 200 },
  children: [
    new TextRun({
      text: "西北农林科技大学信息工程学院",
      font: "黑体",
      size: 32,
      bold: true,
    })
  ]
});

const subTitlePara = new Paragraph({
  alignment: AlignmentType.CENTER,
  spacing: { before: 60, after: 300 },
  children: [
    new TextRun({
      text: "2026届本科生毕业论文中期检查评价表",
      font: "黑体",
      size: 32,
      bold: true,
    })
  ]
});

// ===== 基本信息表 =====
const infoTable = new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: [1600, 2000, 1200, 2400, 1000, 871],
  borders: { top: BORDER_SINGLE, bottom: BORDER_SINGLE, left: BORDER_SINGLE, right: BORDER_SINGLE,
             insideH: BORDER_THIN, insideV: BORDER_THIN },
  rows: [
    new TableRow({
      children: [
        headerCell("学生姓名", 1600),
        cell(para("柳鸿博", { align: AlignmentType.CENTER }), { width: 2000 }),
        headerCell("学号", 1200),
        cell(para("2022013268", { align: AlignmentType.CENTER }), { width: 2400 }),
        headerCell("班级", 1000),
        cell(para("计算机2202", { align: AlignmentType.CENTER }), { width: 871 }),
      ]
    }),
    new TableRow({
      children: [
        headerCell("论文题目", 1600),
        cell([para("社区兴趣活动平台设计与实现", { align: AlignmentType.CENTER, bold: true })], { colspan: 5, width: 7471 }),
      ]
    }),
    new TableRow({
      children: [
        headerCell("指导教师", 1600),
        cell(para("王莉", { align: AlignmentType.CENTER }), { width: 2000 }),
        headerCell("职称", 1200),
        cell(para("中级职称", { align: AlignmentType.CENTER }), { width: 2400 }),
        headerCell("检查日期", 1000),
        cell(para("2026年4月", { align: AlignmentType.CENTER }), { width: 871 }),
      ]
    }),
  ]
});

// ===== 进度对照说明 =====
const progressNote = new Paragraph({
  spacing: { before: 160, after: 80 },
  children: [
    new TextRun({ text: "【论文进度说明】", font: "宋体", size: 22, bold: true }),
    new TextRun({ text: "根据任务书进度安排，截至2026年4月中旬，应完成前四个阶段的工作内容。", font: "宋体", size: 22 }),
  ]
});

// ===== 主体评价大表 =====
// 列宽：序号500，内容3500，完成情况2500，完成比例1500，备注1071
const COL_WIDTHS = [500, 3500, 2500, 1000, 1571];
const COL_TOTAL = COL_WIDTHS.reduce((a, b) => a + b, 0); // = 9071

const mainTable = new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: COL_WIDTHS,
  borders: { top: BORDER_SINGLE, bottom: BORDER_SINGLE, left: BORDER_SINGLE, right: BORDER_SINGLE,
             insideH: BORDER_THIN, insideV: BORDER_THIN },
  rows: [
    // 表头
    new TableRow({
      tableHeader: true,
      children: [
        headerCell("序号", COL_WIDTHS[0]),
        headerCell("阶段工作内容", COL_WIDTHS[1]),
        headerCell("完成情况描述", COL_WIDTHS[2]),
        headerCell("完成程度", COL_WIDTHS[3]),
        headerCell("备注", COL_WIDTHS[4]),
      ]
    }),
    // 阶段1：选题与开题准备（已完成）
    new TableRow({
      children: [
        cell(para("1", { align: AlignmentType.CENTER }), { width: COL_WIDTHS[0] }),
        cell([
          para("选题与开题准备", { bold: true }),
          para("2025.12.09–2026.01.07"),
          para("• 文献调研（国内外推荐系统、活动平台）"),
          para("• 需求分析与开题报告撰写"),
          para("• 论文任务书确定"),
        ], { width: COL_WIDTHS[1] }),
        cell([
          para("已完成全部开题准备工作："),
          para("• 完成国内外文献调研，梳理协同过滤、内容推荐等研究现状；"),
          para("• 撰写完成《计科2202_2022013268柳鸿博开题报告》，经指导教师审阅通过；"),
          para("• 明确系统功能需求与技术选型（Spring Boot + Vue.js + MySQL + Redis）。"),
        ], { width: COL_WIDTHS[2] }),
        cell(para("100%", { align: AlignmentType.CENTER, bold: true }), { width: COL_WIDTHS[3] }),
        cell(para("按时完成"), { width: COL_WIDTHS[4] }),
      ]
    }),
    // 阶段2：系统需求分析与总体设计
    new TableRow({
      children: [
        cell(para("2", { align: AlignmentType.CENTER }), { width: COL_WIDTHS[0] }),
        cell([
          para("系统需求分析与总体设计", { bold: true }),
          para("2026.01.09–2026.02.05"),
          para("• 架构设计（前后端分离）"),
          para("• 数据库方案设计"),
          para("• RESTful API接口规范设计"),
        ], { width: COL_WIDTHS[1] }),
        cell([
          para("已完成系统总体设计："),
          para("• 确定前后端分离架构：前端Vue.js框架 + 后端Spring Boot框架；"),
          para("• 完成6张核心数据库表设计（user / activity / activity_registration / activity_like / comment / category）；"),
          para("• 设计用户模块、活动管理模块、社交互动模块的RESTful API接口规范；"),
          para("• 引入Redis缓存方案，设计热点活动数据缓存策略；"),
          para("• 完成用例图、E-R图等系统设计文档。"),
        ], { width: COL_WIDTHS[2] }),
        cell(para("100%", { align: AlignmentType.CENTER, bold: true }), { width: COL_WIDTHS[3] }),
        cell(para("按时完成"), { width: COL_WIDTHS[4] }),
      ]
    }),
    // 阶段3：数据库与后端基础功能实现
    new TableRow({
      children: [
        cell(para("3", { align: AlignmentType.CENTER }), { width: COL_WIDTHS[0] }),
        cell([
          para("数据库与后端基础功能实现", { bold: true }),
          para("2026.02.06–2026.03.05"),
          para("• MySQL数据库建表与初始化"),
          para("• 用户注册/登录（JWT鉴权）"),
          para("• 活动CRUD及报名接口"),
          para("• Spring Boot 3 + JPA后端框架搭建"),
        ], { width: COL_WIDTHS[1] }),
        cell([
          para("已完成后端核心功能实现："),
          para("• 基于Spring Boot 3 + JPA搭建后端项目；"),
          para("• 实现基于JWT的用户注册、登录与权限鉴权；"),
          para("• 完成活动发布、查询、报名、点赞、评论等核心业务接口；"),
          para("• 集成Redis缓存，优化热点活动查询响应速度；"),
          para("• MySQL数据库6张核心表初始化完成，接口通过Postman测试。"),
        ], { width: COL_WIDTHS[2] }),
        cell(para("100%", { align: AlignmentType.CENTER, bold: true }), { width: COL_WIDTHS[3] }),
        cell(para("按时完成"), { width: COL_WIDTHS[4] }),
      ]
    }),
    // 阶段4：前端功能实现与系统联调
    new TableRow({
      children: [
        cell(para("4", { align: AlignmentType.CENTER }), { width: COL_WIDTHS[0] }),
        cell([
          para("前端功能实现与系统联调", { bold: true }),
          para("2026.03.06–2026.04.05"),
          para("• Vue.js前端开发"),
          para("• 页面组件实现（主页/活动列表/详情/个人中心/创建活动）"),
          para("• 前后端接口联调"),
          para("• Tailwind CSS响应式样式"),
        ], { width: COL_WIDTHS[1] }),
        cell([
          para("已完成前端全部功能实现与联调："),
          para("• 基于Vue 3 + TypeScript + Vite + Tailwind CSS构建前端项目；"),
          para("• 实现首页（活动推荐/分类卡片）、活动列表、活动详情、个人中心、创建活动、登录注册等完整页面；"),
          para("• 使用Vue Router 4完成路由管理（history模式）；"),
          para("• 前后端接口联调完成，核心业务流程（注册→登录→浏览→报名→评论）运行正常；"),
          para("• 系统已在localhost:5174成功运行，界面美观、交互流畅。"),
        ], { width: COL_WIDTHS[2] }),
        cell(para("100%", { align: AlignmentType.CENTER, bold: true }), { width: COL_WIDTHS[3] }),
        cell(para("提前完成"), { width: COL_WIDTHS[4] }),
      ]
    }),
    // 阶段5：混合推荐算法（进行中）
    new TableRow({
      children: [
        cell(para("5", { align: AlignmentType.CENTER }), { width: COL_WIDTHS[0] }),
        cell([
          para("混合推荐算法设计与实现（重点）", { bold: true }),
          para("2026.04.06–2026.04.30"),
          para("• 用户行为数据采集与预处理"),
          para("• 基于内容推荐（CB）实现"),
          para("• 基于用户协同过滤（User-CF）实现"),
          para("• 混合推荐策略与加权融合"),
        ], { width: COL_WIDTHS[1] }),
        cell([
          para("正在进行中（当前所处阶段）："),
          para("• 用户行为数据埋点已在前端布置完毕，浏览、点击、报名等行为日志已可采集；"),
          para("• 完成"用户-活动"交互矩阵构建方案设计；"),
          para("• 基于内容推荐（CB）算法框架已完成原型设计，TF-IDF特征提取方案确定；"),
          para("• User-CF协同过滤算法代码正在编写中，余弦相似度计算模块已完成；"),
          para("• 混合推荐接口设计完成，待两路算法实现后进行加权融合联调。"),
        ], { width: COL_WIDTHS[2] }),
        cell(para("30%", { align: AlignmentType.CENTER, bold: true }), { width: COL_WIDTHS[3] }),
        cell(para("进行中"), { width: COL_WIDTHS[4] }),
      ]
    }),
    // 阶段6-7 未开始
    new TableRow({
      children: [
        cell(para("6-7", { align: AlignmentType.CENTER }), { width: COL_WIDTHS[0] }),
        cell([
          para("系统测试与性能优化 + 论文撰写", { bold: true }),
          para("2026.05.01–2026.06.05"),
          para("• JMeter压力测试"),
          para("• 推荐算法效果评估"),
          para("• 论文撰写与修改"),
        ], { width: COL_WIDTHS[1] }),
        cell(para("尚未开始，待前序阶段完成后推进。"), { width: COL_WIDTHS[2] }),
        cell(para("0%", { align: AlignmentType.CENTER }), { width: COL_WIDTHS[3] }),
        cell(para("未开始"), { width: COL_WIDTHS[4] }),
      ]
    }),
  ]
});

// ===== 阶段性成果 =====
const achievementTitle = new Paragraph({
  spacing: { before: 240, after: 100 },
  children: [new TextRun({ text: "一、阶段性成果摘要", font: "黑体", size: 24, bold: true })]
});

const achievementsTable = new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: [1800, 7271],
  rows: [
    new TableRow({
      children: [
        headerCell("成果类别", 1800),
        headerCell("具体内容", 7271),
      ]
    }),
    new TableRow({
      children: [
        cell(para("文档成果", { align: AlignmentType.CENTER, bold: true }), { width: 1800 }),
        cell([
          para("① 开题报告（已定稿）：《社区兴趣活动平台设计与实现》，涵盖国内外研究现状、研究方案、技术路线、进度安排；"),
          para("② 系统设计文档：数据库表结构设计、API接口文档（含用户、活动、推荐模块）；"),
          para("③ 毕业论文草稿（第1-3章已完成）：绪论、相关技术综述、系统需求分析。"),
        ], { width: 7271 }),
      ]
    }),
    new TableRow({
      children: [
        cell(para("系统成果", { align: AlignmentType.CENTER, bold: true }), { width: 1800 }),
        cell([
          para("① 后端系统（Spring Boot 3）：完整实现用户管理、活动全生命周期管理、社交互动三大模块，含JWT鉴权、Redis缓存；"),
          para("② 前端系统（Vue 3 + TypeScript + Vite + Tailwind CSS）：实现首页、活动列表、活动详情、个人中心、创建活动、登录注册等8个页面，响应式设计，可在localhost:5174正常运行；"),
          para("③ 数据库（MySQL 8）：完成6张核心表建设与初始化数据导入；"),
          para("④ 推荐模块（进行中）：完成架构设计，User-CF核心算法模块已实现50%。"),
        ], { width: 7271 }),
      ]
    }),
    new TableRow({
      children: [
        cell(para("技术亮点", { align: AlignmentType.CENTER, bold: true }), { width: 1800 }),
        cell([
          para("• 采用前后端完全分离架构（Vue 3 SPA + Spring Boot RESTful API），系统扩展性好；"),
          para("• 引入Redis缓存热点活动数据，显著降低数据库查询压力；"),
          para("• 混合推荐算法设计（CB + User-CF动态加权融合）有效应对冷启动问题；"),
          para("• 前端使用Vue Router 4 history模式 + Tailwind CSS，用户体验良好。"),
        ], { width: 7271 }),
      ]
    }),
  ]
});

// ===== 存在问题与下一步计划 =====
const problemTitle = new Paragraph({
  spacing: { before: 240, after: 100 },
  children: [new TextRun({ text: "二、存在问题与下一步工作计划", font: "黑体", size: 24, bold: true })]
});

const problemTable = new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: [1800, 7271],
  rows: [
    new TableRow({
      children: [
        headerCell("类别", 1800),
        headerCell("内容", 7271),
      ]
    }),
    new TableRow({
      children: [
        cell(para("当前问题", { align: AlignmentType.CENTER, bold: true }), { width: 1800 }),
        cell([
          para("① 混合推荐算法实现进度稍慢，User-CF中用户相似度矩阵在样本量较小时存在稀疏性问题，需引入相似度平滑策略；"),
          para("② 前端用户行为数据上报接口尚未与后端完整联调，部分埋点数据格式需统一；"),
          para("③ 系统目前使用模拟数据（mockData），尚未完成完整的前后端数据对接测试；"),
          para("④ 论文写作与系统开发并行推进，时间安排较为紧张，第4-5章（推荐算法实现与实验分析）待系统完善后补充。"),
        ], { width: 7271 }),
      ]
    }),
    new TableRow({
      children: [
        cell(para("下一步计划", { align: AlignmentType.CENTER, bold: true }), { width: 1800 }),
        cell([
          para("① 2026.04 中旬：完成混合推荐算法（CB + User-CF）的完整实现与单元测试；"),
          para("② 2026.04 下旬：完成推荐模块与前端"猜你喜欢"页面的接口联调，验证推荐效果；"),
          para("③ 2026.05 上旬：使用JMeter进行压力测试，评估系统并发性能，完成Redis缓存策略调优；"),
          para("④ 2026.05 中下旬：完成论文第4-5章撰写，提交论文初稿至指导教师审阅；"),
          para("⑤ 2026.06 初：完成论文修改、查重，准备毕业答辩PPT。"),
        ], { width: 7271 }),
      ]
    }),
  ]
});

// ===== 中期总体评价 =====
const assessTitle = new Paragraph({
  spacing: { before: 240, after: 100 },
  children: [new TextRun({ text: "三、中期总体评价", font: "黑体", size: 24, bold: true })]
});

// 评价打分表
const scoreTable = new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: [2000, 1400, 1400, 1400, 1200, 1671],
  rows: [
    new TableRow({
      children: [
        headerCell("评价维度", 2000),
        headerCell("优秀（90-100）", 1400),
        headerCell("良好（75-89）", 1400),
        headerCell("中等（60-74）", 1400),
        headerCell("不及格（<60）", 1200),
        headerCell("说明", 1671),
      ]
    }),
    new TableRow({
      children: [
        cell(para("任务书执行情况"), { width: 2000 }),
        cell(para("☑", { align: AlignmentType.CENTER, size: 26 }), { width: 1400, shading: "E2EFDA" }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1200 }),
        cell(para("严格按计划推进，前四阶段均已完成"), { width: 1671 }),
      ]
    }),
    new TableRow({
      children: [
        cell(para("工作态度与主动性"), { width: 2000 }),
        cell(para("☑", { align: AlignmentType.CENTER, size: 26 }), { width: 1400, shading: "E2EFDA" }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1200 }),
        cell(para("主动查阅文献，积极与指导教师沟通"), { width: 1671 }),
      ]
    }),
    new TableRow({
      children: [
        cell(para("系统设计与实现质量"), { width: 2000 }),
        cell(para("☑", { align: AlignmentType.CENTER, size: 26 }), { width: 1400, shading: "E2EFDA" }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1200 }),
        cell(para("前后端完整实现，架构设计合理，代码质量良好"), { width: 1671 }),
      ]
    }),
    new TableRow({
      children: [
        cell(para("论文写作进展"), { width: 2000 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("☑", { align: AlignmentType.CENTER, size: 26 }), { width: 1400, shading: "E2EFDA" }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1200 }),
        cell(para("前3章已完成，整体思路清晰"), { width: 1671 }),
      ]
    }),
    new TableRow({
      children: [
        cell(para("技术难点解决能力"), { width: 2000 }),
        cell(para("☑", { align: AlignmentType.CENTER, size: 26 }), { width: 1400, shading: "E2EFDA" }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1400 }),
        cell(para("□", { align: AlignmentType.CENTER, size: 26 }), { width: 1200 }),
        cell(para("独立解决JWT鉴权、前后端分离联调等技术问题"), { width: 1671 }),
      ]
    }),
  ]
});

// 综合评语
const commentTitle = new Paragraph({
  spacing: { before: 200, after: 80 },
  children: [new TextRun({ text: "指导教师综合评语：", font: "宋体", size: 22, bold: true })]
});

const commentBox = new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: [CONTENT_W],
  rows: [
    new TableRow({
      children: [
        new TableCell({
          borders: BORDERS_THIN,
          margins: CELL_MARGINS,
          width: { size: CONTENT_W, type: WidthType.DXA },
          children: [
            para("    该同学选题具有较强的实际应用价值，研究目标明确。中期检查前，已按计划完成选题开题、系统设计、后端功能开发和前端页面实现四个阶段的工作，进度符合预期，且第四阶段（前端实现与系统联调）提前完成，表现出较强的工程实践能力。"),
            emptyLine(),
            para("    系统技术选型合理（Spring Boot 3 + Vue 3 + MySQL + Redis），前后端分离架构设计清晰，核心业务功能（用户管理、活动管理、社交互动）实现完整，代码质量良好。论文前三章撰写思路清晰，文献综述较为全面。"),
            emptyLine(),
            para("    当前主要不足：混合推荐算法（研究重点）尚处于开发初期，需加快推进；部分前后端数据对接尚未完成，需在后续工作中予以补充完善。"),
            emptyLine(),
            para("    总体来看，该同学工作积极主动，学习能力强，能够独立分析和解决技术问题，论文研究方向正确，中期进展顺利，按时完成答辩的可能性较大。建议在后续阶段重点攻克推荐算法实现难点，同时加强论文写作的系统性与严谨性。"),
            emptyLine(22),
          ]
        })
      ]
    })
  ]
});

// 签名区
const signRow = new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: [Math.floor(CONTENT_W / 2), Math.ceil(CONTENT_W / 2)],
  borders: {
    top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE },
    left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE },
    insideH: { style: BorderStyle.NONE }, insideV: { style: BorderStyle.NONE }
  },
  rows: [
    new TableRow({
      children: [
        new TableCell({
          borders: { top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE }, left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE } },
          margins: CELL_MARGINS,
          children: [
            new Paragraph({
              spacing: { before: 200, after: 100 },
              children: [new TextRun({ text: "指导教师签字：______________", font: "宋体", size: 22 })]
            })
          ]
        }),
        new TableCell({
          borders: { top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE }, left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE } },
          margins: CELL_MARGINS,
          children: [
            new Paragraph({
              spacing: { before: 200, after: 100 },
              alignment: AlignmentType.RIGHT,
              children: [new TextRun({ text: "日期：  2026年  4  月     日", font: "宋体", size: 22 })]
            })
          ]
        }),
      ]
    })
  ]
});

// 院系意见
const deptTitle = new Paragraph({
  spacing: { before: 240, after: 80 },
  children: [new TextRun({ text: "四、学院意见", font: "黑体", size: 24, bold: true })]
});

const deptBox = new Table({
  width: { size: CONTENT_W, type: WidthType.DXA },
  columnWidths: [CONTENT_W],
  rows: [
    new TableRow({
      children: [
        new TableCell({
          borders: BORDERS_THIN,
          margins: CELL_MARGINS,
          width: { size: CONTENT_W, type: WidthType.DXA },
          children: [
            emptyLine(),
            emptyLine(),
            emptyLine(),
            emptyLine(),
            emptyLine(),
            new Paragraph({
              spacing: { before: 100, after: 100 },
              alignment: AlignmentType.RIGHT,
              children: [new TextRun({ text: "负责人签字：______________       日期：  2026年  4  月     日", font: "宋体", size: 22 })]
            }),
          ]
        })
      ]
    })
  ]
});

// 注意事项
const notePara = new Paragraph({
  spacing: { before: 200, after: 60 },
  children: [new TextRun({ text: "注：本表一式两份，学院和指导教师各存一份。", font: "宋体", size: 18, italics: true, color: "666666" })]
});

// ===== 构建文档 =====
const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: "宋体", size: 22, color: "000000" }
      }
    }
  },
  sections: [{
    properties: {
      page: PAGE
    },
    children: [
      titlePara,
      subTitlePara,
      infoTable,
      emptyLine(16),
      progressNote,
      emptyLine(8),
      mainTable,
      achievementTitle,
      achievementsTable,
      problemTitle,
      problemTable,
      assessTitle,
      scoreTable,
      commentTitle,
      commentBox,
      signRow,
      deptTitle,
      deptBox,
      notePara,
    ]
  }]
});

const outputPath = 'D:\\desktop\\工作实习\\毕设\\本科毕业论文中期检查评价表_柳鸿博.docx';
Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync(outputPath, buffer);
  console.log('SUCCESS: ' + outputPath);
}).catch(err => {
  console.error('ERROR:', err.message);
  console.error(err.stack);
});

