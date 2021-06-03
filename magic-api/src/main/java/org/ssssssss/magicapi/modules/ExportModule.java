package org.ssssssss.magicapi.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.params.ExcelExportEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.ssssssss.magicapi.config.MagicModule;
import org.ssssssss.script.annotation.Comment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 对象集合转Excel字节数组
 *
 * @author 冰点
 * @date 2021-6-2 16:42:16
 */
@Component
public class ExportModule implements MagicModule {
    private static final Logger log = LoggerFactory.getLogger(ExportModule.class);

    /**
     * 将对象转换为excel文件
     *
     * @param columnHeaders
     * @param exportObjList
     * @param title
     * @param sheetName
     * @return
     * @throws IOException
     */
    @Comment("对象转换为Excel文件")
    public static byte[] buildExcelByMap(@Comment("表格列头定义") Map<String, String> columnHeaders, @Comment("导出对象集合") List<Map<String,Object>> exportObjList, @Comment("表格title") String title, @Comment("sheet名称") String sheetName) throws IOException {
        byte[] bytes;
        Workbook workbook = null;
        try {
            List<ExcelExportEntity> colEntity = new ArrayList<>();
            columnHeaders.forEach((key, value) -> colEntity.add(new ExcelExportEntity(value, key)));
            ExportParams param = new ExportParams(title, sheetName);
            workbook = ExcelExportUtil.exportExcel(param, colEntity, exportObjList);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            bytes = bos.toByteArray();
        } catch (IOException e) {
            throw new IOException("转换Excel文件异常", e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
        return bytes;
    }


    /**
     * 将对象集合转成excel并下载
     * @param columnHeaders 列头
     * @param exportObjList 导出对象
     * @param title 文件名
     * @param sheetName sheet名
     * @return
     * @throws IOException
     */
    @Comment("Excel文件导出")
    public static ResponseEntity<?> excel(@Comment("表格列头定义") Map<String, String> columnHeaders, @Comment("导出对象集合") List<Map<String,Object>> exportObjList, @Comment("表格title") String title, @Comment("sheet名称") String sheetName) throws IOException {
        Object value = buildExcelByMap(columnHeaders, exportObjList, title, sheetName);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(title, "UTF-8") + ".xls")
                .body(value);
    }
    /**
     * 将对象集合转成excel并下载
     * @param columnHeaders 列头
     * @param exportObjList 导出对象
     * @param title 文件名
     * @return
     * @throws IOException
     */
    @Comment("Excel文件导出")
    public static ResponseEntity<?> excel(@Comment("表格列头定义") Map<String, String> columnHeaders, @Comment("导出对象集合") List<Map<String,Object>> exportObjList, @Comment("表格title") String title) throws IOException {
        Object value = buildExcelByMap(columnHeaders, exportObjList, title, "");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(title, "UTF-8") + ".xls")
                .body(value);
    }

    /**
     * 获取模块名
     */
    @Override
    public String getModuleName() {
        return "export";
    }
}