/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.meta.objectmodel;

import equa.code.operations.FactoryMethod;
import equa.meta.traceability.Source;

/**
 *
 * @author frankpeeters
 */
public class Factory extends SingletonObjectType {

    private static final long serialVersionUID = 1L;

    private final ObjectType product;

    public Factory(FactType parent, String factoryName, ObjectType factoryProduct,
            Source source) {
        super(parent, factoryName, source);
        this.product = factoryProduct;
    }

    public ObjectType getProduct() {
        return product;
    }

    @Override
    void generateMethods() {
       // generateSingletonField();
        if (!product.isAbstract()) {
            codeClass.addOperation(new FactoryMethod(this, product));
        }
        for (ObjectType subtype : product.concreteSubTypes()) {
            codeClass.addOperation(new FactoryMethod(this, subtype));
        }
    }
}
