// This is a generated file. Not intended for manual editing.
package org.javamaster.mybatis.tools.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.javamaster.mybatis.tools.psi.impl.*;

public interface MybatisParamTypes {

  IElementType ATTR = new MybatisParamElementType("ATTR");
  IElementType ATTRS = new MybatisParamElementType("ATTRS");
  IElementType ATTR_NAME = new MybatisParamElementType("ATTR_NAME");
  IElementType ATTR_VALUE = new MybatisParamElementType("ATTR_VALUE");
  IElementType FIELD_NAME = new MybatisParamElementType("FIELD_NAME");

  IElementType ATTR_NAME_LABEL = new MybatisParamTokenType("ATTR_NAME_LABEL");
  IElementType ATTR_SEPARATOR = new MybatisParamTokenType("ATTR_SEPARATOR");
  IElementType ATTR_VALUE_LABEL = new MybatisParamTokenType("ATTR_VALUE_LABEL");
  IElementType FIELD_NAME_LABEL = new MybatisParamTokenType("FIELD_NAME_LABEL");
  IElementType SEPARATOR = new MybatisParamTokenType("SEPARATOR");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ATTR) {
        return new MybatisParamAttrImpl(node);
      }
      else if (type == ATTRS) {
        return new MybatisParamAttrsImpl(node);
      }
      else if (type == ATTR_NAME) {
        return new MybatisParamAttrNameImpl(node);
      }
      else if (type == ATTR_VALUE) {
        return new MybatisParamAttrValueImpl(node);
      }
      else if (type == FIELD_NAME) {
        return new MybatisParamFieldNameImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
