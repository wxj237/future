package io.renren.modules.sys.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * 性别转换器
 */
public class GenderConverter implements Converter<Integer> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Integer convertToJavaData(com.alibaba.excel.metadata.data.ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        String stringValue = cellData.getStringValue();
        if ("男".equals(stringValue)) {
            return 0;
        } else if ("女".equals(stringValue)) {
            return 1;
        } else if ("保密".equals(stringValue)) {
            return 2;
        }
        return 0;
    }

    @Override
    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        switch (value) {
            case 0:
                return new WriteCellData<>("男");
            case 1:
                return new WriteCellData<>("女");
            case 2:
                return new WriteCellData<>("保密");
            default:
                return new WriteCellData<>("");
        }
    }
}