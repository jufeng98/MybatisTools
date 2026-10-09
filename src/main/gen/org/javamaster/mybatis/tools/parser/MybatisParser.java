// This is a generated file. Not intended for manual editing.
package org.javamaster.mybatis.tools.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.javamaster.mybatis.tools.psi.MybatisTypes.*;
import static org.javamaster.mybatis.tools.parser.MybatisParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class MybatisParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return mybatisFile(b, l + 1);
  }

  /* ********************************************************** */
  // attrName ATTR_SEPARATOR attrValue
  public static boolean attr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr")) return false;
    if (!nextTokenIs(b, ATTR_NAME_LABEL)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ATTR, null);
    r = attrName(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, ATTR_SEPARATOR));
    r = p && attrValue(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ATTR_NAME_LABEL
  public static boolean attrName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attrName")) return false;
    if (!nextTokenIs(b, "<attr name>", ATTR_NAME_LABEL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTR_NAME, "<attr name>");
    r = consumeToken(b, ATTR_NAME_LABEL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ATTR_VALUE_LABEL
  public static boolean attrValue(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attrValue")) return false;
    if (!nextTokenIs(b, "<attr value>", ATTR_VALUE_LABEL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTR_VALUE, "<attr value>");
    r = consumeToken(b, ATTR_VALUE_LABEL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (attr SEPARATOR?)*
  public static boolean attrs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attrs")) return false;
    Marker m = enter_section_(b, l, _NONE_, ATTRS, "<attrs>");
    while (true) {
      int c = current_position_(b);
      if (!attrs_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "attrs", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // attr SEPARATOR?
  private static boolean attrs_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attrs_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attr(b, l + 1);
    r = r && attrs_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // SEPARATOR?
  private static boolean attrs_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attrs_0_1")) return false;
    consumeToken(b, SEPARATOR);
    return true;
  }

  /* ********************************************************** */
  // FIELD_NAME_LABEL
  public static boolean fieldName(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fieldName")) return false;
    if (!nextTokenIs(b, "<field name>", FIELD_NAME_LABEL)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FIELD_NAME, "<field name>");
    r = consumeToken(b, FIELD_NAME_LABEL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // fieldName (SEPARATOR attrs)?
  static boolean mybatisFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mybatisFile")) return false;
    if (!nextTokenIs(b, FIELD_NAME_LABEL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fieldName(b, l + 1);
    r = r && mybatisFile_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (SEPARATOR attrs)?
  private static boolean mybatisFile_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mybatisFile_1")) return false;
    mybatisFile_1_0(b, l + 1);
    return true;
  }

  // SEPARATOR attrs
  private static boolean mybatisFile_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mybatisFile_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEPARATOR);
    r = r && attrs(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

}
