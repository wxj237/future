-- 重置日报周报系统数据

-- 清空现有日报数据
DELETE FROM daily_plan;

-- 清空现有周报数据
DELETE FROM weekly_report;

-- 重置自增ID
ALTER TABLE daily_plan AUTO_INCREMENT = 1;
ALTER TABLE weekly_report AUTO_INCREMENT = 1;