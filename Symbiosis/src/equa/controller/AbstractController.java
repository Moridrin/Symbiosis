package equa.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractController implements IController {

    protected final List<IView> registeredViews;
    protected List<IModel> registeredModels;

    public AbstractController() {
        registeredViews = Collections.synchronizedList(new ArrayList<IView>());
        registeredModels = new ArrayList<>();
    }

    @Override
    public void propertyChanged() {
        // TODO Auto-generated method stub
    }

    /**
     * to get the view count, used for debugging purposes
     *
     * @return number of views registered with the controller.
     */
    public int getViewCount() {
        return registeredViews.size();
    }

    public void addModel(IModel model) {
        if (!registeredModels.contains(model)) {
            registeredModels.add(model);
        }
    }

    public void removeModel(IModel model) {
        if (registeredModels.contains(model)) {
            registeredModels.remove(model);
        }
    }

    public void addView(IView view) {
        if (!registeredViews.contains(view)) {

            registeredViews.add(view);
        }
    }

    public void removeView(IView view) {
        registeredViews.remove(view);
    }
}
