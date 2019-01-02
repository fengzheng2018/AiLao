package com.android.ailao.describe;

import android.os.Environment;
import android.text.TextUtils;

import com.android.ailao.data.MyDescribe;
import com.android.ailao.data.MyRecord;

import org.litepal.LitePal;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SaveDescribe {

    public void saveText(String text){
        // 去掉首尾空格
        String text1 = text.trim();
        if( !TextUtils.isEmpty(text1) ) {

            // 创建存储文件夹
            boolean mkDirSuccess = true;
            String txtPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "AiLaoShan" + File.separator + "Text" + File.separator;
            File folder = new File(txtPath);
            if (!folder.exists()) {
                mkDirSuccess = folder.mkdirs();
            }

            // 查找未完成的记录
            long recordId = 0;
            List<MyRecord> myRecords = LitePal.where("isOver=?", "1").find(MyRecord.class);
            if (myRecords.size() > 0) {
                MyRecord myRecord = myRecords.get(0);
                recordId = myRecord.getRecordId();
            }
            // 没有未完成的记录，新建一次记录
            else {
                recordId = System.currentTimeMillis();
                //向MyRecord表中添加一条记录
                MyRecord myRecord = new MyRecord();
                myRecord.setIsOver(1);
                myRecord.setRecordId(recordId);
                myRecord.save();
            }

            // 创建文件夹是否成功
            if (mkDirSuccess) {
                // 存在未完成的记录
                if (recordId != 0) {
                    // 生成文件名
                    String textName = System.currentTimeMillis() + ".txt";
                    // 往数据库添加记录
                    MyDescribe myDescribe = new MyDescribe();
                    myDescribe.setRecordId(recordId);
                    myDescribe.setTxtName(textName);
                    myDescribe.save();

                    // 文件存本地
                    String filePath = txtPath + textName;
                    File txtFile = new File(filePath);
                    try {
                        boolean createTxtSuccess = txtFile.createNewFile();
                        if (createTxtSuccess) {
                            FileWriter fw = new FileWriter(txtFile);
                            BufferedWriter bufferedWriter = new BufferedWriter(fw);
                            bufferedWriter.write(text);
                            bufferedWriter.flush();
                            bufferedWriter.close();
                            fw.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
