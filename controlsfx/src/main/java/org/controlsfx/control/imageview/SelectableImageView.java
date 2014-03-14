package org.controlsfx.control.imageview;

import static org.controlsfx.tools.MathTools.isInInterval;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

import org.controlsfx.tools.rectangle.Rectangles2D;

/**
 * A {@code SelectableImageView} is (in the colloquial not the inheritance sense) an
 * {@link javafx.scene.image.ImageView ImageView} which allows the user to select an area of the image in the typical
 * manner used by picture editors:
 * <p>
 * While holding the left mouse key down, a rectangular selection can be drawn onto the image. This selection can be
 * moved, resized in eight cardinal directions and removed. Additionally, the selection's ratio can be fixed in which
 * case the user's resizing will be limited such that the ratio is always upheld.
 * 
 * <h3>Screenshots</h3>
 * TODO
 * 
 * <h3>Code Samples</h3>
 * 
 * {@code SelectableImageView} offers the same constructors as {@code ImageView}. All additional functionality regarding
 * the selection must be set using the typical accessor functions.
 * <p>
 * The following snippet creates a new instance with the ControlsFX logo loaded from the web, sets a selected area and
 * fixes its ratio:
 * 
 * <pre>
 * SelectableImageView selectableImageView =
 *         new SelectableImageView(&quot;http://cache.fxexperience.com/wp-content/uploads/2013/05/ControlsFX.png&quot;);
 * selectableImageView.setSelection(33, 50, 100, 100);
 * selectableImageView.setFixedSelectionRatio(1); // (this is actually the default value)
 * selectableImageView.setSelectionRatioFixed(true);
 * </pre>
 * 
 * <h3>Image</h3>
 * 
 * To display an image the control provides the {@link #imageProperty() image} and {@link #preserveImageRatioProperty()
 * preserveImageRatio} properties. Both are used in the same way as in the {@link javafx.scene.image.ImageView
 * ImageView} class:
 * <ul>
 * <li> {@link javafx.scene.image.ImageView#imageProperty() ImageView.imageProperty()}</li>
 * <li> {@link javafx.scene.image.ImageView#preserveRatioProperty() ImageView.preserveRatioProperty()}<br>
 * The name was slightly adapted by adding the word <i>Image</i> to discern this ratio from the selection ratio (see
 * below).</li>
 * </ul>
 * 
 * <h3>Size & Alignment</h3>
 * 
 * The control will grow to fill all the available space and the value of the {@link #preserveImageRatioProperty()
 * preserveImageRatio} property defines how the displayed image will fill this control's space. If it is set to
 * {@code false}, the image will be resized to fill the whole control which will generally include skewing it. If it is
 * set to {@code true}, the image will be resized to the maximal size while preserving its ratio as well as fitting into
 * the control and will then be centered within it.
 * 
 * <h3>Selection</h3>
 * 
 * The selected area is represented by the {@link SelectableImageView#selectionProperty() selection} property. It
 * contains a {@link Rectangle2D}, which is immutable so the selection can only be changed by setting a new one. The
 * rectangle's coordinates are interpreted relative to the image.
 * <p>
 * The selection is only displayed if it is valid and active (see below).
 * 
 * <h4>Valid</h4>
 * 
 * If a selection is not fully contained in the bounds of the current image, it is invalid. This will be indicated by
 * the {@link #selectionValidProperty selectionValid} property.
 * 
 * <h4>Active</h4>
 * 
 * The {@link SelectableImageView#selectionActiveProperty() selectionActive} property indicates whether the selection is
 * currently active.
 * <p>
 * By default this property is managed by this control which means the selection is automatically activated when a new
 * non-{@code null} selection is set and deactivated when a {@code null} selection is set. But this makes it impossible
 * to unidirectly bind this property to another one (setting the value of a unidirectionally bound property throws an
 * exception). If the user wishes to manage the activity state manually he can do so by setting the
 * {@link #selectionActivityExplicitlyManagedProperty() selectionActivityExplicitlyManaged} property's value to
 * {@code true}. In that case this control will never change the activity state.
 * 
 * <h4>Changing</h4>
 * 
 * The selection can be changed by simply setting a new value in the {@link SelectableImageView#selectionProperty()
 * selection} property. But it can also change due to user interaction with the control. During the latter changes the
 * {@link #selectionChangingProperty() selectionChanging} property is set to {@code true}.
 * 
 * <h4>Ratio</h4>
 * 
 * The selection's ratio can be fixed with the {@link #selectionRatioFixedProperty() selectionRatioFixed} and the
 * {@link #fixedSelectionRatioProperty() fixedSelectionRatio} properties. The former indicates whether the ratio is
 * currently fixed. The latter contains the ratio as a {@code double} which must be strictly positive and is interpreted
 * as {@code width / height}. Their default values are {@code false} and {@code 1.0}.
 * <p>
 * A fixed ratio becomes important in the context of user interaction with the selection. If the ratio is fixed, the
 * user's changes of the selection are limited in order to uphold the specified ratio.
 * <p>
 * If a selection exists and the {@link #selectionRatioFixedProperty() selectionRatioFixed} property is set to
 * {@code true}, the selection is immediately resized to the currently set ratio. Similarly, if a selection exists and
 * its ratio is fixed, setting a new value for the {@link #fixedSelectionRatioProperty() fixedSelectionRatio} property
 * resizes the selection to the new ratio.
 * <p>
 * If a selection is explicitly set, its ratio <em>not</em> checked and hence not changed to match a possibly fixed
 * ratio!
 */
public class SelectableImageView extends Control {

    /* ************************************************************************
     *                                                                         *
     * Attributes & Properties                                                 *
     *                                                                         *
     **************************************************************************/

    // IMAGE VIEW

    /**
     * The {@link Image} to be painted by this {@code SelectableImageView}.
     */
    private final ObjectProperty<Image> image;

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * control's bounding box.
     */
    private final BooleanProperty preserveImageRatio;

    // SELECTION

    /**
     * The selected area as a rectangle. The coordinates are interpreted relative to the currently shown image.
     */
    private final ObjectProperty<Rectangle2D> selection;

    /**
     * Indicates whether the current selection is valid. This is the case if the {@link #imageProperty() image} and
     * {@link #selectionProperty() selection} properties are not null and the selection rectangle lies within the bounds
     * of the image.
     * <p>
     * A selection will only be displayed if it is valid (and active).
     */
    private final BooleanProperty selectionValid;

    /**
     * Indicates whether an area is currently selected. A selection will only be displayed if it is active (and valid).
     * <p>
     * If {@link #selectionActivityExplicitlyManagedProperty() selectionActivityExplicitlyManaged} is set to
     * {@code false} (which is the default) this control will update this property immediately after a new
     * {@link #selectionProperty() selection} is set: if the new selection is {@code null}, it will be set to false;
     * otherwise to {@code true}. <br>
     * If {@code selectionActivityExplicitlyManaged} is {@code true} this control will never change this property's
     * value. In this case it must be managed by the using code but it is possible to unidirectionally bind it to
     * another property.
     */
    private final BooleanProperty selectionActive;

    /**
     * Indicates whether the {@link #selection} is currently changing due to GUI interaction. This will be set to
     * {@code true} when changing the selection begins and set to {@code false} when it ends.
     */
    private final BooleanProperty selectionChanging;

    /**
     * Indicates whether the ratio of the selection will be fixed. When the value changes from {@code false} to
     * {@code true} and a selection exists, the value of the {@link #fixedSelectionRatioProperty() selectionRatio}
     * property will immediately be enforced so consider setting it first.
     */
    private final BooleanProperty selectionRatioFixed;

    /**
     * The fixed ratio of the selection interpreted as {@code width / height}. If {@link #selectionRatioFixedProperty()
     * selectionRatioFixed} is {@code true}, this ratio will be upheld by all changes made by GUI interaction. This
     * explicitly excludes setting the {@link #selectionProperty() selection} property directly in which case the
     * selection's ratio will not be checked!
     * <p>
     * Only strictly positive values are allowed as ratio, otherwise an {@link IllegalArgumentException} is thrown.
     */
    private final DoubleProperty fixedSelectionRatio;

    // META

    /**
     * Indicates whether the {@link #selectionActiveProperty() selectionActive} property will be explicitly managed by
     * the code using this control. This can be useful if the {@code selectionActive} property should be bound to
     * another property.
     */
    private final BooleanProperty selectionActivityExplicitlyManaged;

    /* ************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new SelectableImageView.
     */
    public SelectableImageView() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        // IMAGE VIEW
        this.image = new SimpleObjectProperty<Image>(this, "imageProperty");
        this.preserveImageRatio = new SimpleBooleanProperty(this, "preserveImageRatioProperty", false);

        // SELECTION
        this.selection = new SimpleObjectProperty<Rectangle2D>(this, "selectionProperty");
        this.selectionValid = new SimpleBooleanProperty(this, "selectionValidProperty", false);
        this.selectionActive = new SimpleBooleanProperty(this, "selectionActiveProperty", false);
        this.selectionChanging = new SimpleBooleanProperty(this, "selectionChangingProperty", false);

        this.selectionRatioFixed = new SimpleBooleanProperty(this, "selectionRatioFixedProperty", false);
        this.fixedSelectionRatio = new SimpleDoubleProperty(this, "fixedSelectionRatioProperty", 1) {
            @Override
            public void set(double newValue) {
                if (newValue <= 0)
                    throw new IllegalArgumentException("The fixed selection ratio must be positive.");
                super.set(newValue);
            }
        };

        // META
        this.selectionActivityExplicitlyManaged =
                new SimpleBooleanProperty(this, "selectionActivityExplicitlyManagedProperty", false);

        addStateUpdatingListeners();
    }

    /**
     * Adds listeners to the properties which update the control's state.
     */
    private void addStateUpdatingListeners() {
        // valid & active
        image.addListener(new ChangeListener<Image>() {
            @Override
            public void changed(ObservableValue<? extends Image> observable, Image oldValue, Image newValue) {
                updateSelectionValidity();
            }
        });
        selection.addListener(new ChangeListener<Rectangle2D>() {
            @Override
            public void changed(
                    ObservableValue<? extends Rectangle2D> observable, Rectangle2D oldValue, Rectangle2D newValue) {
                updateSelectionValidity();
                updateSelectionActiviteState();
            }
        });

        // ratio
        selectionRatioFixed.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                boolean valueChangedToTrue = !oldValue && newValue;
                if (valueChangedToTrue)
                    fixSelectionRatio();
            }
        });
        fixedSelectionRatio.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (isSelectionRatioFixed())
                    fixSelectionRatio();
            }
        });
    }

    /**
     * Creates a new SelectableImageView using the specified image.
     * 
     * @param image
     *            the image to show after construction
     */
    public SelectableImageView(Image image) {
        this();
        setImage(image);
    }

    /**
     * Creates a new SelectableImageView using the image loaded from the specified URL.
     * 
     * @param url
     *            the string representing the URL from which to load the image
     */
    public SelectableImageView(String url) {
        this();
        Image image = new Image(url);
        setImage(image);
    }

    /* ************************************************************************
     *                                                                         *
     * Model State                                                             *
     *                                                                         *
     **************************************************************************/

    /**
     * Fixes the ratio of the current selection (if it exists).
     */
    private void fixSelectionRatio() {
        boolean noSelectionToFix = getImage() == null || !isSelectionValid();
        if (noSelectionToFix)
            return;

        Rectangle2D resizeBounds = new Rectangle2D(0, 0, getImage().getWidth(), getImage().getHeight());
        Rectangle2D resizedSelection = Rectangles2D.fixRatioWithinBounds(
                getSelection(), getFixedSelectionRatio(), resizeBounds);
        setSelection(resizedSelection);
    }

    /**
     * Evaluates the current state of this control and sets the {@link #selectionValidProperty() selectionValid}
     * property accordingly.
     */
    private void updateSelectionValidity() {
        if (getImage() == null || getSelection() == null)
            selectionValid.set(false);
        else {
            boolean upperLeftInImage =
                    isInInterval(0, getSelection().getMinX(), getImage().getWidth()) &&
                    isInInterval(0, getSelection().getMinY(), getImage().getHeight());
            boolean lowerRightInImage =
                    isInInterval(0, getSelection().getMaxX(), getImage().getWidth()) &&
                    isInInterval(0, getSelection().getMaxY(), getImage().getHeight());

            selectionValid.set(upperLeftInImage && lowerRightInImage);
        }
    }

    /**
     * Updates the {@link #selectionActiveProperty() selectionActive} property if the
     * {@link #selectionActivityExplicitlyManagedProperty() selectionActivityExplicitlyManaged} property indicates that
     * it is not explicitly managed by the using code.
     */
    private void updateSelectionActiviteState() {
        boolean explicitlyManaged = isSelectionActivityExplicitlyManaged();
        if (explicitlyManaged)
            return;

        boolean selectionActive = getSelection() != null;
        setSelectionActive(selectionActive);
    }

    /* ************************************************************************
     *                                                                         *
     * Style Sheet & Skin Handling                                             *
     *                                                                         *
     **************************************************************************/

    /**
     * The name of the style class used in CSS for instances of this class.
     */
    private static final String DEFAULT_STYLE_CLASS = "selectable-image-view";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAgentStylesheet() {
        return getClass().getResource("selectableimageview.css").toExternalForm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new SelectableImageViewSkin(this);
    }

    /* ************************************************************************
     *                                                                         *
     * Property Access                                                         *
     *                                                                         *
     **************************************************************************/

    // IMAGE VIEW

    /**
     * The {@link Image} to be painted by this {@code SelectableImageView}.
     * 
     * @return the image as a property
     */
    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    /**
     * The {@link Image} to be painted by this {@code SelectableImageView}.
     * 
     * @return the image
     */
    public Image getImage() {
        return imageProperty().get();
    }

    /**
     * The {@link Image} to be painted by this {@code SelectableImageView}.
     * 
     * @param image
     *            the image to set
     */
    public void setImage(Image image) {
        imageProperty().set(image);
    }

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * control's bounding box.
     * 
     * @return the preserveImageRatio as a property
     */
    public BooleanProperty preserveImageRatioProperty() {
        return preserveImageRatio;
    }

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * control's bounding box.
     * 
     * @return the preserveImageRatio
     */
    public boolean isPreserveImageRatio() {
        return preserveImageRatioProperty().get();
    }

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * control's bounding box.
     * 
     * @param preserveImageRatio
     *            the preserveImageRatio to set
     */
    public void setPreserveImageRatio(boolean preserveImageRatio) {
        preserveImageRatioProperty().set(preserveImageRatio);
    }

    // SELECTION

    /**
     * The selected area as a rectangle. The coordinates are interpreted relative to the currently shown image.
     * <p>
     * This property should not be unidirectionally bound to another one because new values will be set by this control
     * when the user interacts with the selection.
     * 
     * @return the selection as a property
     */
    public ObjectProperty<Rectangle2D> selectionProperty() {
        return selection;
    }

    /**
     * The selected area as a rectangle. The coordinates are interpreted relative to the currently shown image.
     * 
     * @return the selection
     */
    public Rectangle2D getSelection() {
        return selectionProperty().get();
    }

    /**
     * The selected area as a rectangle. The coordinates are interpreted relative to the currently shown image.
     * 
     * @param selection
     *            the selection to set
     */
    public void setSelection(Rectangle2D selection) {
        selectionProperty().set(selection);
    }

    /**
     * Sets the selected area as the rectangle's upper left point's coordinates and the rectangle's width and height.
     * The coordinates are interpreted relative to the currently shown image.
     * 
     * @param upperLeftX
     *            the x coordinate of the selection's upper left point
     * @param upperLeftY
     *            the y coordinate of the selection's upper left point
     * @param width
     *            the selection's width
     * @param height
     *            the selection's height
     * 
     */
    public void setSelection(double upperLeftX, double upperLeftY, double width, double height) {
        selectionProperty().set(new Rectangle2D(upperLeftX, upperLeftY, width, height));
    }

    /**
     * Indicates whether the current selection is valid. This is the case if the {@link #imageProperty() image} and
     * {@link #selectionProperty() selection} properties are not null and the selection rectangle lies within the bounds
     * of the image.
     * 
     * @return the selectionValid as a property
     */
    public ReadOnlyBooleanProperty selectionValidProperty() {
        return selectionValid;
    }

    /**
     * Indicates whether the current selection is valid. This is the case if the {@link #imageProperty() image} and
     * {@link #selectionProperty() selection} properties are not null and the selection rectangle lies within the bounds
     * of the image.
     * 
     * @return the selectionValid
     */
    public boolean isSelectionValid() {
        return selectionValidProperty().get();
    }

    /**
     * Indicates whether an area is currently selected. A selection will only be displayed if it is active (and valid).
     * <p>
     * If {@link #selectionActivityExplicitlyManagedProperty() selectionActivityExplicitlyManaged} is set to
     * {@code false} (which is the default) this control will update this property immediately after a new
     * {@link #selectionProperty() selection} is set: if the new selection is {@code null}, it will be set to false;
     * otherwise to {@code true}. <br>
     * If {@code selectionActivityExplicitlyManaged} is {@code true} this control will never change this property's
     * value. In this case it must be managed by the using code but it is possible to unidirectionally bind it to
     * another property.
     * 
     * @return the selectionActive as a property
     */
    public BooleanProperty selectionActiveProperty() {
        return selectionActive;
    }

    /**
     * Indicates whether an area is currently selected. A selection will only be displayed if it is active (and valid).
     * <p>
     * If {@link #selectionActivityExplicitlyManagedProperty() selectionActivityExplicitlyManaged} is set to
     * {@code false} (which is the default) this control will update this property immediately after a new
     * {@link #selectionProperty() selection} is set: if the new selection is {@code null}, it will be set to false;
     * otherwise to {@code true}. <br>
     * If {@code selectionActivityExplicitlyManaged} is {@code true} this control will never change this property's
     * value. In this case it must be managed by the using code but it is possible to unidirectionally bind it to
     * another property.
     * 
     * @return whether the selection is active
     */
    public boolean isSelectionActive() {
        return selectionActiveProperty().get();
    }

    /**
     * Indicates whether an area is currently selected. A selection will only be displayed if it is active (and valid).
     * <p>
     * If {@link #selectionActivityExplicitlyManagedProperty() selectionActivityExplicitlyManaged} is set to
     * {@code false} (which is the default) this control will update this property immediately after a new
     * {@link #selectionProperty() selection} is set: if the new selection is {@code null}, it will be set to false;
     * otherwise to {@code true}. <br>
     * If {@code selectionActivityExplicitlyManaged} is {@code true} this control will never change this property's
     * value. In this case it must be managed by the using code, e.g. by unidirectionally binding it to another
     * property.
     * 
     * @param selectionActive
     *            the new selection active status
     */
    public void setSelectionActive(boolean selectionActive) {
        selectionActiveProperty().set(selectionActive);
    }

    /**
     * Indicates whether the {@link #selection} is currently changing due to GUI interaction. This will be set to
     * {@code true} when changing the selection begins and set to {@code false} when it ends.
     * <p>
     * This property should not be unidirectionally bound to another one because new values will be set by this control
     * when the user interacts with the selection.
     * 
     * @return the selectionChanging as a property
     */
    public BooleanProperty selectionChangingProperty() {
        // TODO It would be very nice if this could be a read only property
        // but it is unclear how it could then be edited by 'SelectableImageViewBehavior'.
        return selectionChanging;
    }

    /**
     * Indicates whether the {@link #selection} is currently changing due to GUI interaction. This will be set to
     * {@code true} when changing the selection begins and set to {@code false} when it ends.
     * 
     * @return the selectionChanging
     */
    public boolean isSelectionChanging() {
        return selectionChangingProperty().get();
    }

    /**
     * Indicates whether the ratio of the selection will be fixed. When the value changes from {@code false} to
     * {@code true} and a selection exists, the value of the {@link #fixedSelectionRatioProperty() selectionRatio}
     * property will immediately be enforced so consider setting it first.
     * 
     * @return the selectionRatioFixed as a property
     */
    public BooleanProperty selectionRatioFixedProperty() {
        return selectionRatioFixed;
    }

    /**
     * Indicates whether the ratio of the selection will be fixed.
     * 
     * @return the selectionRatioFixed
     */
    public boolean isSelectionRatioFixed() {
        return selectionRatioFixedProperty().get();
    }

    /**
     * Indicates whether the ratio of the selection will be fixed. When the value changes from {@code false} to
     * {@code true} and a selection exists, the value of the {@link #fixedSelectionRatioProperty() fixedSelectionRatio}
     * property will immediately be enforced so consider setting it first.
     * 
     * @param selectionRatioFixed
     *            the selectionRatioFixed to set
     */
    public void setSelectionRatioFixed(boolean selectionRatioFixed) {
        selectionRatioFixedProperty().set(selectionRatioFixed);
    }

    /**
     * The fixed ratio of the selection interpreted as {@code width / height}. If {@link #selectionRatioFixedProperty()
     * selectionRatioFixed} is {@code true}, this ratio will be upheld by all changes made by GUI interaction. This
     * explicitly excludes setting the {@link #selectionProperty() selection} property directly in which case the
     * selection's ratio will not be checked!
     * <p>
     * Only strictly positive values are allowed as ratio, otherwise an {@link IllegalArgumentException} is thrown.
     * 
     * @return the fixedSelectionRatio as a property
     */
    public DoubleProperty fixedSelectionRatioProperty() {
        return fixedSelectionRatio;
    }

    /**
     * The fixed ratio of the selection interpreted as {@code width / height}. If {@link #selectionRatioFixedProperty()
     * selectionRatioFixed} is {@code true}, this ratio will be upheld by all changes made by GUI interaction. This
     * explicitly excludes setting the {@link #selectionProperty() selection} property directly in which case the
     * selection's ratio will not be checked!
     * <p>
     * This will only ever return strictly positive values.
     * 
     * @return the fixedSelectionRatio
     */
    public double getFixedSelectionRatio() {
        return fixedSelectionRatioProperty().get();
    }

    /**
     * The fixed ratio of the selection interpreted as {@code width / height}. If {@link #selectionRatioFixedProperty()
     * selectionRatioFixed} is {@code true}, this ratio will be upheld by all changes made by GUI interaction. This
     * explicitly excludes setting the {@link #selectionProperty() selection} property directly in which case the
     * selection's ratio will not be checked!
     * <p>
     * Only strictly positive values are allowed as ratio, otherwise an {@link IllegalArgumentException} is thrown.
     * 
     * @param fixedSelectionRatio
     *            the fixedSelectionRatio to set
     * @throws IllegalArgumentException
     *             if {@code fixedSelectionRatio} is not strictly positive
     */
    public void setFixedSelectionRatio(double fixedSelectionRatio) {
        fixedSelectionRatioProperty().set(fixedSelectionRatio);
    }

    // META

    /**
     * Indicates whether the {@link #selectionActiveProperty() selectionActive} property will be explicitly managed by
     * the code using this control. This can be useful if the {@code selectionActive} property should be bound to
     * another property.
     * 
     * @return the selectionActivityExplicitlyManaged as a property
     */
    public BooleanProperty selectionActivityExplicitlyManagedProperty() {
        return selectionActivityExplicitlyManaged;
    }

    /**
     * Indicates whether the {@link #selectionActiveProperty() selectionActive} property will be explicitly managed by
     * the code using this control. This can be useful if the {@code selectionActive} property should be bound to
     * another property.
     * 
     * @return the selectionActivityExplicitlyManaged
     */
    public boolean isSelectionActivityExplicitlyManaged() {
        return selectionActivityExplicitlyManagedProperty().get();
    }

    /**
     * Indicates whether the {@link #selectionActiveProperty() selectionActive} property will be explicitly managed by
     * the code using this control. This can be useful if the {@code selectionActive} property should be bound to
     * another property.
     * 
     * @param selectionActivityExplicitlyManaged
     *            the selectionActivityExplicitlyManaged to set
     */
    public void setSelectionActivityExplicitlyManaged(boolean selectionActivityExplicitlyManaged) {
        selectionActivityExplicitlyManagedProperty().set(selectionActivityExplicitlyManaged);
    }

}
