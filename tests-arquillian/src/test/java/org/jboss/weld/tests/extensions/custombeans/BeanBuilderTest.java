/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.extensions.custombeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class BeanBuilderTest {

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class).addPackage(BeanBuilderTest.class.getPackage())
                .addAsServiceProvider(Extension.class, BuilderExtension.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCustomBean(BeanManager beanManager) throws Exception {
        Set<Bean<?>> beans = beanManager.getBeans("bar");
        assertEquals(1, beans.size());
        Bean<Foo> fooBean = (Bean<Foo>) beans.iterator().next();
        assertEquals(Dependent.class, fooBean.getScope());
        Foo foo1 = (Foo) beanManager.getReference(fooBean, Foo.class, beanManager.createCreationalContext(fooBean));
        Foo foo2 = (Foo) beanManager.getReference(fooBean, Foo.class, beanManager.createCreationalContext(fooBean));
        assertFalse(foo1.getId().equals(foo2.getId()));

        beans = beanManager.getBeans(Foo.class, Juicy.Literal.INSTANCE);
        assertEquals(1, beans.size());
        fooBean = (Bean<Foo>) beans.iterator().next();
        Foo foo = (Foo) beanManager.getReference(fooBean, Foo.class, beanManager.createCreationalContext(fooBean));
        foo.ping();

        beans = beanManager.getBeans(Integer.class, Random.Literal.INSTANCE);
        assertEquals(1, beans.size());
        Bean<Integer> randomBean = (Bean<Integer>) beans.iterator().next();
        CreationalContext<Integer> ctx = beanManager.createCreationalContext(randomBean);
        Integer random = (Integer) beanManager.getReference(randomBean, Integer.class, ctx);
        assertNotNull(random);
        assertTrue(random >= 0 && random < 1000);
        randomBean.destroy(random, ctx);
        assertTrue(BuilderExtension.DISPOSED.get());

        beans = beanManager.getBeans(Long.class, AnotherRandom.Literal.INSTANCE);
        assertEquals(1, beans.size());
        Bean<Long> anotherRandomBean = (Bean<Long>) beans.iterator().next();
        Long anotherRandom = (Long) beanManager.getReference(anotherRandomBean, Long.class,
                beanManager.createCreationalContext(anotherRandomBean));
        assertNotNull(anotherRandom);
        assertEquals(Long.valueOf(foo.getId() * 2), anotherRandom);
    }

}
