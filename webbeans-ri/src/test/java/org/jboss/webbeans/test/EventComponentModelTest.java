package org.jboss.webbeans.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.webbeans.Dependent;
import javax.webbeans.Event;
import javax.webbeans.Standard;

import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.injectable.ComponentConstructor;
import org.jboss.webbeans.introspector.SimpleAnnotatedField;
import org.jboss.webbeans.model.EventComponentModel;
import org.jboss.webbeans.test.annotations.AnotherDeploymentType;
import org.jboss.webbeans.test.components.DangerCall;
import org.jboss.webbeans.test.mock.MockManagerImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the component model used only for the container supplied
 * Event component.
 * 
 * @author David Allen
 *
 */
public class EventComponentModelTest
{
   private MockManagerImpl manager = null;
   private EventComponentModel<EventImpl<DangerCall>> eventComponentModel = null;
   EventImpl<DangerCall> eventModelField = null;

   @BeforeMethod
   public void before() throws Exception
   {
      List<Class<? extends Annotation>> enabledDeploymentTypes = new ArrayList<Class<? extends Annotation>>();
      enabledDeploymentTypes.add(Standard.class);
      enabledDeploymentTypes.add(AnotherDeploymentType.class);
      manager = new MockManagerImpl(enabledDeploymentTypes);
      Field eventModelField = this.getClass().getDeclaredField("eventModelField");
      eventComponentModel = new EventComponentModel<EventImpl<DangerCall>>(
            new SimpleAnnotatedField<EventImpl<DangerCall>>(eventModelField),
            new SimpleAnnotatedField<EventImpl<DangerCall>>(eventModelField),
            manager);

   }

   /**
    * The name should always be null since this type of component is not allowed to have a name.
    */
   @Test(groups = "eventbus")
   public void testName()
   {
      assert eventComponentModel.getName() == null;
   }
   
   /**
    * The scope type should always be @Dependent
    */
   @Test(groups = "eventbus")
   public void testScopeType()
   {
      assert eventComponentModel.getScopeType().equals(Dependent.class);
   }
   
   /**
    * The deployment type should always be @Standard
    */
   @Test(groups = "eventbus")
   public void testDeploymentType()
   {
      assert eventComponentModel.getDeploymentType().equals(Standard.class);
   }
   
   @Test(groups = "eventbus")
   public void testApiTypes()
   {
      Set<Class<?>> apis = eventComponentModel.getApiTypes();
      assert apis.size() >= 1;
      for (Class<?> api : apis)
      {
         api.equals(Event.class);
      }
   }
   
   @Test(groups = "eventbus")
   public void testConstructor()
   {
      ComponentConstructor<EventImpl<DangerCall>> constructor = eventComponentModel.getConstructor();
      assert constructor != null;
      Event<DangerCall> event = constructor.invoke(manager);
      assert event != null;
   }
}
