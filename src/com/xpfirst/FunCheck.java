package com.xpfirst;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaojindan
 * @date 2019/4/3 0003 16:17
 * @des
 */
public class FunCheck extends AnAction{
    private final MyToolWin myToolWin = new MyToolWin();

    /**
     * 执行插件的入口，相当于java中的main方法
     */
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        // 获取当前的project对象
        Project project = anActionEvent.getProject();
        // 获取当前文件对象
        Editor editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        String fileName = psiFile.getName();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileName);
        //定义tree 的根目录
        DefaultMutableTreeNode lineTreeNode = new DefaultMutableTreeNode("行数超过80");
        // 遍历当前对象的所有属性
        for (PsiElement psiElement : psiFile.getChildren()) {
            System.out.println(psiElement);
            if (psiElement instanceof PsiClass){
                PsiClass psiClass = (PsiClass) psiElement;
                // 方法列表
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod psiMethod : methods) {
                    // 获取备注
                    PsiCodeBlock psiCodeBlock = psiMethod.getBody();
                    // 获取大括号里的内容
                    String codeText = psiCodeBlock.getText();
                    long lineCount = getLineNumberByIo(codeText);
                    System.out.println("行数:" + lineCount);
                    // 行数大于80了,发出警告
                    if (lineCount > 2){
                        // 行号
                        int lineNumbers = document.getLineNumber(psiMethod.getTextOffset());
                        String text = psiMethod.getName()+"方法超过80行(line "+(lineNumbers+1)+")";
                        DefaultMutableTreeNode tmpTreeNode = new DefaultMutableTreeNode(text);
                        lineTreeNode.add(tmpTreeNode);
                    }

                }
            }
        }
        // 显示输出内容
        fileNode.add(lineTreeNode);
        myToolWin.addLineNode(fileNode);
        myToolWin.showToolWin(project);

    }

    /**   得到字符串中的行数  使用io*/
    public long getLineNumberByIo(String str){
        LineNumberReader lnr = new LineNumberReader(new CharArrayReader(str.toCharArray()));
        try {
            lnr.skip(Long.MAX_VALUE);
            lnr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lnr.getLineNumber() + 1;
    }
}
