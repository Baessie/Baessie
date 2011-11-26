import xml.transform.{RewriteRule, RuleTransformer}
import xml.{Node, NodeSeq}

object Rewrite {
  def rewriter(f: PartialFunction[Node, NodeSeq]): RuleTransformer = new RuleTransformer(rule(f))

  def rule(f: PartialFunction[Node, NodeSeq]): RewriteRule = new RewriteRule {
    override def transform(n: Node) = if (f.isDefinedAt(n)) f(n) else n
  }
}
