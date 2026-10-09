// This is a generated file. Not intended for manual editing.
package org.javamaster.mybatis.tools.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.javamaster.mybatis.tools.psi.impl.*;

public interface MybatisTypes {

  IElementType ATTR = new MybatisElementType("ATTR");
  IElementType ATTRS = new MybatisElementType("ATTRS");
  IElementType ATTR_NAME = new MybatisElementType("ATTR_NAME");
  IElementType ATTR_VALUE = new MybatisElementType("ATTR_VALUE");
  IElementType FIELD_NAME = new MybatisElementType("FIELD_NAME");

  IElementType ATTR_NAME_LABEL = new MybatisTokenType("ATTR_NAME_LABEL");
  IElementType ATTR_SEPARATOR = new MybatisTokenType("ATTR_SEPARATOR");
  IElementType ATTR_VALUE_LABEL = new MybatisTokenType("ATTR_VALUE_LABEL");
  IElementType FIELD_NAME_LABEL = new MybatisTokenType("FIELD_NAME_LABEL");
  IElementType SEPARATOR = new MybatisTokenType("SEPARATOR");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ATTR) {
        return new MybatisAttrImpl(node);
      }
      else if (type == ATTRS) {
        return new MybatisAttrsImpl(node);
      }
      else if (type == ATTR_NAME) {
        return new MybatisAttrNameImpl(node);
      }
      else if (type == ATTR_VALUE) {
        return new MybatisAttrValueImpl(node);
      }
      else if (type == FIELD_NAME) {
        return new MybatisFieldNameImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
