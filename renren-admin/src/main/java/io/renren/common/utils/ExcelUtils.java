/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * excel工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
public class ExcelUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    /**
     * Excel导出
     *
     * @param response  response
     * @param fileName  文件名
     * @param sheetName sheetName
     * @param list      数据List
     * @param pojoClass 对象Class
     */
    public static void exportExcel(HttpServletResponse response, String fileName, String sheetName, List<?> list,
                                   Class<?> pojoClass) throws IOException {
        // 如果文件名为空，则使用默认文件名
        if (StrUtil.isBlank(fileName)) {
            fileName = "日报数据";
        }

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        // 确保文件名正确编码
        fileName = URLUtil.encode(fileName, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), pojoClass).registerConverter(new LongStringConverter()).sheet(sheetName).doWrite(list);
    }

    /**
     * Excel导出，先sourceList转换成List<targetClass>，再导出
     *
     * @param response    response
     * @param fileName    文件名
     * @param sheetName   sheetName
     * @param sourceList  原数据List
     * @param targetClass 目标对象Class
     */
    public static void exportExcelToTarget(HttpServletResponse response, String fileName, String sheetName, List<?> sourceList,
                                           Class<?> targetClass) throws Exception {
        try {
            List targetList = new ArrayList<>(sourceList.size());
            for (Object source : sourceList) {
                // 添加空值检查
                if (source == null) {
                    logger.warn("Excel导出数据源中存在空对象，跳过处理");
                    continue;
                }
                
                Object target = targetClass.newInstance();
                BeanUtils.copyProperties(source, target);
                targetList.add(target);
            }

            exportExcel(response, fileName, sheetName, targetList, targetClass);
        } catch (Exception e) {
            logger.error("Excel导出过程中发生异常", e);
            logger.error("数据源大小: {}, 目标类: {}", sourceList != null ? sourceList.size() : 0, targetClass.getName());
            throw e;
        }
    }

}