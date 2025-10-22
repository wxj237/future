-- 修复权限配置问题，确保admin用户能正常访问系统

-- 确保admin用户具有超级管理员权限
UPDATE sys_user SET super_admin = 1 WHERE username = 'admin';

-- 创建一个超级管理员角色
INSERT INTO sys_role (id, name, remark, dept_id, creator, create_date, updater, update_date) 
VALUES (1, '超级管理员', '系统内置角色，拥有所有权限', null, 1067246875800000001, now(), 1067246875800000001, now())
ON DUPLICATE KEY UPDATE update_date = now();

-- 将admin用户分配到超级管理员角色
INSERT INTO sys_role_user (id, role_id, user_id, creator, create_date) 
VALUES (1, 1, 1067246875800000001, 1067246875800000001, now())
ON DUPLICATE KEY UPDATE create_date = now();

-- 确保角色菜单关系完整（为超级管理员角色分配所有菜单）
INSERT INTO sys_role_menu (role_id, menu_id, creator, create_date)
SELECT 1, id, 1067246875800000001, now() FROM sys_menu
ON DUPLICATE KEY UPDATE create_date = now();

-- 清除可能存在的缓存问题
DELETE FROM sys_user_token WHERE user_id = 1067246875800000001;