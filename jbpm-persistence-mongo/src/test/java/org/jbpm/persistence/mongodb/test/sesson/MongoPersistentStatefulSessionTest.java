/*
 * Copyright 2011 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.persistence.mongodb.test.sesson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.drools.core.SessionConfiguration;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.command.impl.FireAllRulesInterceptor;
import org.drools.core.command.impl.LoggingInterceptor;
import org.drools.persistence.SingleSessionCommandService;
import org.jbpm.persistence.mongodb.MongoSingleSessionCommandService;
import org.jbpm.persistence.mongodb.test.AbstractMongoBaseTest;
import org.jbpm.persistence.mongodb.test.object.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class MongoPersistentStatefulSessionTest extends AbstractMongoBaseTest {

    private static Logger logger = LoggerFactory.getLogger(MongoPersistentStatefulSessionTest.class);
    

    @Parameters
    public static Collection<Object[]> persistence() {
        Object[][] locking = new Object[][] { { false }, { true } };
        return Arrays.asList(locking);
    };
    
    public MongoPersistentStatefulSessionTest(boolean locking) {}
    
    @Test
    public void testFactHandleSerialization() {
        String str = "";
        str += "package org.kie.test\n";
        str += "import java.util.concurrent.atomic.AtomicInteger\n";
        str += "global java.util.List list\n";
        str += "rule rule1\n";
        str += "when\n";
        str += " $i: AtomicInteger(intValue > 0)\n";
        str += "then\n";
        str += " list.add( $i );\n";
        str += "end\n";
        str += "\n";

        /*
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRL );
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        if ( kbuilder.hasErrors() ) {
            fail( kbuilder.getErrors().toString() );
        }

        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        */
        createBaseWithResourceStrings(ResourceType.DRL, str);

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        assertTrue(ksession.getId()>0);
        System.out.println("ksessionid = " + ksession.getId());
        List<?> list = new ArrayList<Object>();

        ksession.setGlobal( "list", list );
        System.out.println("ksessionid = " + ksession.getId());
        
        

        AtomicInteger value = new AtomicInteger(4);
        FactHandle atomicFH = ksession.insert( value );

        ksession.fireAllRules();

        assertEquals( 1,
                      list.size() );

        value.addAndGet(1);
        ksession.update(atomicFH, value);
        ksession.fireAllRules();
        
        assertEquals( 2,
                list.size() );
        String externalForm = atomicFH.toExternalForm();

        System.out.println("ksessionid = " + ksession.getId());
        saveSession();
        ksession = reloadKnowledgeSession();
        System.out.println("ksessionid = " + ksession.getId());
        System.out.println("get ksession = " + getKSession().getId());
        
        atomicFH = ksession.execute(CommandFactory.fromExternalFactHandleCommand(externalForm));
        
        value.addAndGet(1);
        ksession.update(atomicFH, value);
        
        ksession.fireAllRules();
        
        list = (List<?>) ksession.getGlobal("list");
        
        assertEquals( 3,
                list.size() );
        
    }
    
    @Test
    public void testLocalTransactionPerStatement() {
        String str = "";
        str += "package org.kie.test\n";
        str += "global java.util.List list\n";
        str += "rule rule1\n";
        str += "when\n";
        str += "  Integer(intValue > 0)\n";
        str += "then\n";
        str += "  list.add( 1 );\n";
        str += "end\n";
        str += "\n";

        createBaseWithResourceStrings(ResourceType.DRL, str);

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        List<?> list = new ArrayList<Object>();

        ksession.setGlobal( "list",
                            list );

        ksession.insert( 1 );
        ksession.insert( 2 );
        ksession.insert( 3 );

        ksession.fireAllRules();

        assertEquals( 3,
                      list.size() );

    }

    @Test
    public void testUserTransactions() throws Exception {
        String str = "";
        str += "package org.kie.test\n";
        str += "global java.util.List list\n";
        str += "rule rule1\n";
        str += "when\n";
        str += "  $i : Integer(intValue > 0)\n";
        str += "then\n";
        str += "  list.add( $i );\n";
        str += "end\n";
        str += "\n";

        createBaseWithResourceStrings(ResourceType.DRL, str);

        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        ut.commit();

        List<?> list = new ArrayList<Object>();

        // insert and commit
        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        ksession.setGlobal( "list",
                            list );
        ksession.insert( 1 );
        ksession.insert( 2 );
        ksession.fireAllRules();
        ut.commit();

        // insert and rollback
        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        ksession.insert( 3 );
        ut.commit();

        // check we rolled back the state changes from the 3rd insert
        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        ksession.fireAllRules();
        ut.commit();
        assertEquals( 3, list.size() );

        // insert and commit
        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        ksession.insert( 3 );
        ksession.insert( 4 );
        ut.commit();

        // rollback again, this is testing that we can do consecutive rollbacks and commits without issue
        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        ksession.insert( 5 );
        ksession.insert( 6 );
        ut.commit();

        ksession.fireAllRules();

        assertEquals( 6, list.size() );
        
        // now load the ksession
        ksession = reloadKnowledgeSession();
        
        ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        ksession.insert( 7 );
        ksession.insert( 8 );
        ut.commit();

        ksession.fireAllRules();

        assertEquals( 14, list.size() );
    }

    @Test
    public void testInterceptor() {
        String str = "";
        str += "package org.kie.test\n";
        str += "global java.util.List list\n";
        str += "rule rule1\n";
        str += "when\n";
        str += "  Integer(intValue > 0)\n";
        str += "then\n";
        str += "  list.add( 1 );\n";
        str += "end\n";
        str += "\n";

        createBaseWithResourceStrings(ResourceType.DRL, str);

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        MongoSingleSessionCommandService sscs = (MongoSingleSessionCommandService)
            ((CommandBasedStatefulKnowledgeSession) ksession).getCommandService();
        sscs.addInterceptor(new LoggingInterceptor());
        sscs.addInterceptor(new FireAllRulesInterceptor());
        sscs.addInterceptor(new LoggingInterceptor());
        List<?> list = new ArrayList<Object>();
        ksession.setGlobal( "list", list );
        ksession.insert( 1 );
        ksession.insert( 2 );
        ksession.insert( 3 );
        ksession.getWorkItemManager().completeWorkItem(0, null);
        assertEquals( 3, list.size() );
    }
    
    @Test
    public void testSetFocus() {
        String str = "";
        str += "package org.kie.test\n";
        str += "global java.util.List list\n";
        str += "rule rule1\n";
        str += "agenda-group \"badfocus\"";
        str += "when\n";
        str += "  Integer(intValue > 0)\n";
        str += "then\n";
        str += "  list.add( 1 );\n";
        str += "end\n";
        str += "\n";
    
        createBaseWithResourceStrings(ResourceType.DRL, str);

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        List<?> list = new ArrayList<Object>();
    
        ksession.setGlobal( "list",
                            list );
    
        ksession.insert( 1 );
        ksession.insert( 2 );
        ksession.insert( 3 );
        ksession.getAgenda().getAgendaGroup("badfocus").setFocus();
    
        ksession.fireAllRules();
    
        assertEquals( 3,
                      list.size() );
    }
    
    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testSharedReferences() {
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        Person x = new Person(new Long(10),  "test" );
		List test = new ArrayList();
        List test2 = new ArrayList();
        test.add( x );
        test2.add( x );

        assertSame( test.get( 0 ), test2.get( 0 ) );

        ksession.insert( test );
        ksession.insert( test2 );
        ksession.fireAllRules();

        StatefulKnowledgeSession ksession2 = reloadKnowledgeSession();

        Iterator c = ksession2.getObjects().iterator();
        List ref1 = (List) c.next();
        List ref2 = (List) c.next();

        assertSame( ref1.get( 0 ), ref2.get( 0 ) );

    }

    @Test
    public void testMergeConfig() {
        // JBRULES-3155
    	createBase();
        Properties properties = new Properties();
        properties.put("drools.processInstanceManagerFactory", "org.jbpm.persistence.mongodb.test.CustomExampleProcessInstanceManagerFactory");
        KieSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);

        setConfig(config);
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        SessionConfiguration sessionConfig = (SessionConfiguration)ksession.getSessionConfiguration();

        assertEquals("org.jbpm.persistence.mongodb.test.CustomExampleProcessInstanceManagerFactory", sessionConfig.getProcessInstanceManagerFactory());
    }

    @Test
    public void testCreateAndDestroySession() {
        String str = "";
        str += "package org.kie.test\n";
        str += "global java.util.List list\n";
        str += "rule rule1\n";
        str += "when\n";
        str += "  Integer(intValue > 0)\n";
        str += "then\n";
        str += "  list.add( 1 );\n";
        str += "end\n";
        str += "\n";

        createBaseWithResourceStrings(ResourceType.DRL, str);

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        List<?> list = new ArrayList<Object>();

        ksession.setGlobal( "list",
                list );

        ksession.insert( 1 );
        ksession.insert( 2 );
        ksession.insert( 3 );

        ksession.fireAllRules();

        assertEquals( 3, list.size() );

        //int ksessionId = ksession.getId();
        ksession.destroy();

        try {
            StatefulKnowledgeSession ksession2 = reloadKnowledgeSession();
            //fail("There should not be any session with id " + ksessionId);
        } catch (Exception e) {

        }
    }

    @Test
    public void testCreateAndDestroyNonPersistentSession() {
        String str = "";
        str += "package org.kie.test\n";
        str += "global java.util.List list\n";
        str += "rule rule1\n";
        str += "when\n";
        str += "  Integer(intValue > 0)\n";
        str += "then\n";
        str += "  list.add( 1 );\n";
        str += "end\n";
        str += "\n";

        createBaseWithResourceStrings(ResourceType.DRL, str);

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        List<?> list = new ArrayList<Object>();

        ksession.setGlobal( "list",
                list );

        ksession.insert( 1 );
        ksession.insert( 2 );
        ksession.insert( 3 );

        ksession.fireAllRules();

        assertEquals( 3, list.size() );

        int ksessionId = ksession.getId();
        ksession.destroy();

        try {
            ksession.fireAllRules();
            fail("Session should already be disposed " + ksessionId);
        } catch (IllegalStateException e) {

        }
    }
}