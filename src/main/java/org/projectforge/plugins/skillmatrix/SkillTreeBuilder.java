/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.TableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.NodeBorder;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.NodeModel;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.ListSelectActionPanel;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SkillTreeBuilder implements Serializable
{
  private static final long serialVersionUID = -5283360078497855756L;

  private final Behavior theme = new WindowsTheme();

  private TableTree<SkillNode, String> tree;

  private SkillDao skillDao;

  private AbstractSecuredPage parentPage;

  private Integer highlightedSkillNodeId;

  private boolean selectMode, showRootNode, showOrders;

  // private AccessChecker accessChecker;
  //
  // private TaskFormatter taskFormatter;
  //
  // private PriorityFormatter priorityFormatter;
  //
  // private UserFormatter userFormatter;
  //
  // private DateTimeFormatter dateTimeFormatter;
  //
  // private UserGroupCache userGroupCache;

  private ISelectCallerPage caller;

  private String selectProperty;

  @SuppressWarnings("serial")
  public AbstractTree<SkillNode> createTree(final String id, final AbstractSecuredPage parentPage, final SkillFilter skillFilter)
  {
    this.parentPage = parentPage;
    final List<IColumn<SkillNode, String>> columns = createColumns();

    tree = new TableTree<SkillNode, String>(id, columns, new SkillTreeProvider(skillFilter).setShowRootNode(showRootNode),
        Integer.MAX_VALUE, SkillTreeExpansion.getExpansionModel()) {
      private static final long serialVersionUID = 1L;

      @Override
      protected Component newContentComponent(final String id, final IModel<SkillNode> model)
      {
        return SkillTreeBuilder.this.newContentComponent(id, this, model);
      }

      @Override
      protected Item<SkillNode> newRowItem(final String id, final int index, final IModel<SkillNode> model)
      {
        return new OddEvenItem<SkillNode>(id, index, model);
      }
    };
    tree.getTable().addTopToolbar(new HeadersToolbar<String>(tree.getTable(), null));
    tree.getTable().addBottomToolbar(new NoRecordsToolbar(tree.getTable()));
    tree.add(new Behavior() {
      @Override
      public void onComponentTag(final Component component, final ComponentTag tag)
      {
        theme.onComponentTag(component, tag);
      }

      @Override
      public void renderHead(final Component component, final IHeaderResponse response)
      {
        theme.renderHead(component, response);
      }
    });
    tree.getTable().add(AttributeModifier.append("class", "tableTree"));
    return tree;
  }

  /**
   * @return
   */
  @SuppressWarnings("serial")
  private List<IColumn<SkillNode, String>> createColumns()
  {
    // final SkillTree skillTree = getSkillTree();
    final CellItemListener<SkillNode> cellItemListener = new CellItemListener<SkillNode>() {
      public void populateItem(final Item<ICellPopulator<SkillNode>> item, final String componentId, final IModel<SkillNode> rowModel)
      {
        final SkillNode skillNode = rowModel.getObject();
        SkillListPage.appendCssClasses(item, skillNode.getSkill(), highlightedSkillNodeId);
      }
    };
    final List<IColumn<SkillNode, String>> columns = new ArrayList<IColumn<SkillNode, String>>();

    // Dummy
    //    columns.add(new TreeColumn<SkillNode, String>(new Model<String>("dummy")) {
    //      @Override
    //      public void populateItem(final Item<ICellPopulator<SkillNode>> cellItem, final String componentId, final IModel<SkillNode> rowModel)
    //      {
    //        super.populateItem(cellItem, componentId, rowModel);
    //      }
    //    });

    columns.add(new TreeColumn<SkillNode, String>(new ResourceModel("plugins.skillmatrix.skillrating.skill")) {
      @Override
      public void populateItem(final Item<ICellPopulator<SkillNode>> cellItem, final String componentId, final IModel<SkillNode> rowModel)
      {
        final RepeatingView view = new RepeatingView(componentId);
        cellItem.add(view);
        final SkillNode skillNode = rowModel.getObject();
        if (selectMode == false) {
          view.add(new ListSelectActionPanel(view.newChildId(), rowModel, SkillEditPage.class, skillNode.getId(), parentPage, ""));
        } else {
          view.add(new ListSelectActionPanel(view.newChildId(), rowModel, caller, selectProperty, skillNode.getId(), ""));
        }
        AbstractListPage.addRowClick(cellItem);
        final NodeModel<SkillNode> nodeModel = (NodeModel<SkillNode>) rowModel;
        final Component nodeComponent = getTree().newNodeComponent(view.newChildId(), nodeModel.getWrappedModel());
        nodeComponent.add(new NodeBorder(nodeModel.getBranches()));
        view.add(nodeComponent);
        cellItemListener.populateItem(cellItem, componentId, rowModel);
      }
    });
    return columns;
  }

  protected void addColumn(final WebMarkupContainer parent, final Component component, final String cssStyle)
  {
    if (cssStyle != null) {
      component.add(AttributeModifier.append("style", new Model<String>(cssStyle)));
    }
    parent.add(component);
  }

  /**
   * @param id
   * @param model
   * @return
   */
  @SuppressWarnings("serial")
  protected Component newContentComponent(final String id, final TableTree<SkillNode, String> tree, final IModel<SkillNode> model)
  {
    return new Folder<SkillNode>(id, tree, model) {

      @Override
      protected IModel< ? > newLabelModel(final IModel<SkillNode> model)
      {
        return new PropertyModel<String>(model, "skill.title");
      }
    };
  }

  /**
   * @param selectMode the selectMode to set
   * @return this for chaining.
   */
  public SkillTreeBuilder setSelectMode(final boolean selectMode)
  {
    this.selectMode = selectMode;
    return this;
  }

  /**
   * @param showRootNode the showRootNode to set
   * @return this for chaining.
   */
  public SkillTreeBuilder setShowRootNode(final boolean showRootNode)
  {
    this.showRootNode = showRootNode;
    return this;
  }

  // /**
  // * @param showOrders the showOrders to set
  // * @return this for chaining.
  // */
  // public SkillTreeBuilder setShowOrders(final boolean showOrders)
  // {
  // this.showOrders = showOrders;
  // return this;
  // }
  //
  // public SkillTreeBuilder set(final AccessChecker accessChecker, final SkillDao skillDao, final SkillFormatter skillFormatter,
  // final PriorityFormatter priorityFormatter, final UserFormatter userFormatter, final DateTimeFormatter dateTimeFormatter,
  // final UserGroupCache userGroupCache)
  // {
  // this.accessChecker = accessChecker;
  // this.skillFormatter = skillFormatter;
  // this.priorityFormatter = priorityFormatter;
  // this.userFormatter = userFormatter;
  // this.dateTimeFormatter = dateTimeFormatter;
  // this.userGroupCache = userGroupCache;
  // this.dateTimeFormatter = dateTimeFormatter;
  // return this;
  // }

  /**
   * @param caller the caller to set
   * @return this for chaining.
   */
  public SkillTreeBuilder setCaller(final ISelectCallerPage caller)
  {
    this.caller = caller;
    return this;
  }

  /**
   * @param selectProperty the selectProperty to set
   * @return this for chaining.
   */
  public SkillTreeBuilder setSelectProperty(final String selectProperty)
  {
    this.selectProperty = selectProperty;
    return this;
  }

  /**
   * @param highlightedSkillNodeId the highlightedSkillNodeId to set
   * @return this for chaining.
   */
  public SkillTreeBuilder setHighlightedSkillNodeId(final Integer highlightedSkillNodeId)
  {
    this.highlightedSkillNodeId = highlightedSkillNodeId;
    final SkillNode node = getSkillTree().getSkillNodeById(highlightedSkillNodeId);
    if (node == null) {
      // Shouldn't occur.
      return this;
    }
    // Open all ancestor nodes of the highlighted node:
    final Set<SkillNode> set = SkillTreeExpansion.getExpansionModel().getObject();
    SkillNode parent = node.getParent();
    while (parent != null) {
      set.add(parent);
      parent = parent.getParent();
    }
    return this;
  }

  /**
   * @param skillDao the skillDao to set
   * @return this for chaining.
   */
  public void setSkillDao(final SkillDao skillDao)
  {
    this.skillDao = skillDao;
  }

  private SkillTree getSkillTree()
  {
    return skillDao.getSkillTree();
  }

}
