package com.xpfirst;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

/*
 * @author gaojindan
 * @date 2019/4/3 0003 16:17
 * @des
 */
public class FunCheck extends AnAction{
    private final MyToolWin myToolWin = new MyToolWin();

    //root node
    private final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("检查结果");
    //行node
    private final DefaultMutableTreeNode lineNode = new DefaultMutableTreeNode("行数超过80");
    //注释node
    private final DefaultMutableTreeNode commentNode = new DefaultMutableTreeNode("注释不规则");

    /**
     * @author gaojindan
     * @date 2019/3/11 0011 17:08
     * @des 流程列表
     * @param  anActionEvent:目录ID
     * @return
     */
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        rootNode.removeAllChildren();
        lineNode.removeAllChildren();
        commentNode.removeAllChildren();
        // 获取当前的project对象
        Project project = anActionEvent.getProject();
        // 获取当前文件对象
        Editor editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        String fileName = psiFile.getName();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileName);
        // 遍历当前对象的所有属性
        for (PsiElement psiElement : psiFile.getChildren()) {
            System.out.println(psiElement);

            if (psiElement instanceof PsiClass){
                PsiClass psiClass = (PsiClass) psiElement;

                // 获取注释
                PsiComment classComment = null;
                for (PsiElement tmpEle : psiClass.getChildren()) {
                    if (tmpEle instanceof PsiComment){
                        classComment = (PsiComment) tmpEle;
                        int lineNumbers = document.getLineNumber(classComment.getTextOffset());
                        // 注释的内容
                        String tmpText = classComment.getText();
                        if (tmpText.indexOf("* @author") < 0){
                            // 没有找到作者
                            String text = psiClass.getName()+"类没有找到作者(line "+(lineNumbers+1)+")";
                            addCommentNode(text);
                        }
                        if (tmpText.indexOf("* @date") < 0 &&tmpText.indexOf("* @time") < 0){
                            // 没有找到日期
                            String text = psiClass.getName()+"类没有找到日期(line "+(lineNumbers+1)+")";
                            addCommentNode(text);
                        }
                        if (tmpText.indexOf("* @des") < 0 &&tmpText.indexOf("* @describe") < 0){
                            // 没有找到描述
                            String text = psiClass.getName()+"类没有找到描述(line "+(lineNumbers+1)+")";
                            addCommentNode(text);
                        }
                    }
                }
                if (classComment == null){
                    // 没有注释
                    int lineNumbers = document.getLineNumber(psiClass.getTextOffset());
                    String text = psiClass.getName()+"类没有注释(line "+(lineNumbers+1)+")";
                    DefaultMutableTreeNode tmpTreeNode = new DefaultMutableTreeNode(text);
                    commentNode.add(tmpTreeNode);
                }
                // 方法列表
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod psiMethod : methods) {
                    // 获取备注

                    // 获取大括号里的内容
                    PsiCodeBlock psiCodeBlock = psiMethod.getBody();
                    String codeText = psiCodeBlock.getText();
                    long lineCount = getLineNumberByIo(codeText);
                    System.out.println("行数:" + lineCount);
                    // 行数大于80了,发出警告
                    if (lineCount > 2){
                        // 行号
                        int lineNumbers = document.getLineNumber(psiMethod.getTextOffset());
                        String text = psiMethod.getName()+"方法超过80行(line "+(lineNumbers+1)+")";
                        DefaultMutableTreeNode tmpTreeNode = new DefaultMutableTreeNode(text);
                        lineNode.add(tmpTreeNode);
                    }

                }
            }
        }
        // 显示输出内容
        rootNode.add(lineNode);
        rootNode.add(commentNode);
        fileNode.add(rootNode);
        myToolWin.addLineNode(fileNode);
        myToolWin.showToolWin(project);

    }

    /**
     * @author gaojindan
     * @date 2019/3/11 0011 17:08
     * @des 添加注释node
     * @param  text:标题
     * @return
     */
    private void addCommentNode(String text){
        DefaultMutableTreeNode tmpTreeNode = new DefaultMutableTreeNode(text);
        commentNode.add(tmpTreeNode);
    }

    /**
     * @author gaojindan
     * @date 2019/3/11 0011 17:08
     * @des 流程列表
     * @param  str:目录ID
     * @return 行数
     */
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
