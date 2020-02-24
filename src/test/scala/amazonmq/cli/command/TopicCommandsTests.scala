/*
 * Copyright 2020 Anton Wierenga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package amazonmq.cli.command

import amazonmq.cli.command.CommandsTests._
import amazonmq.cli.command.TopicCommandsTests._
import amazonmq.cli.util.Console._
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Before, BeforeClass, Test}

class TopicCommandsTests {

  @Before
  def before = {
    assertTrue(shell.executeCommand("remove-all-topics --force").isSuccess)
  }

  @Test
  def testTopicsEmpty = {
    assertEquals(warn(s"No topics found"), shell.executeCommand("list-topics --filter testTopic").getResult)
  }

  @Test
  def testAddTopic = {
    assertEquals(info("Topic 'testTopic' added"), shell.executeCommand("add-topic --name testTopic").getResult)
    assertEquals(
      """|  Topic Name  Enqueued  Dequeued
         |  ----------  --------  --------
         |  testTopic   0         0
         |
         |Total topics: 1""".stripMargin,
      shell.executeCommand("list-topics --filter testTopic").getResult
    )
  }

  @Test
  def testRemoveTopic = {
    assertEquals(info("Topic 'testTopic' added"), shell.executeCommand("add-topic --name testTopic").getResult)
    assertEquals(info("Topic 'testTopic' removed"), shell.executeCommand("remove-topic --name testTopic --force").getResult)
    assertEquals(warn(s"No topics found"), shell.executeCommand("list-topics --filter testTopic").getResult)
  }

  @Test
  def testRemoveAllTopics = {
    assertEquals(info("Topic 'testTopic1' added"), shell.executeCommand("add-topic --name testTopic1").getResult)
    assertEquals(info("Topic 'testTopic2' added"), shell.executeCommand("add-topic --name testTopic2").getResult)
    assertEquals(
      """|Topic removed: 'ActiveMQ.Advisory.Topic'
         |Topic removed: 'testTopic1'
         |Topic removed: 'testTopic2'
         |Total topics removed: 3""".stripMargin,
      shell.executeCommand("remove-all-topics --force").getResult
    )

    assertEquals(warn(s"No topics found"), shell.executeCommand("list-topics --filter testTopic").getResult)
  }

  @Test
  def testRemoveAllTopicsDryRun = {
    assertEquals(info("Topic 'testTopic' added"), shell.executeCommand("add-topic --name testTopic").getResult)
    assertEquals(
      """|Topic to be removed: 'ActiveMQ.Advisory.Topic'
         |Topic to be removed: 'testTopic'
         |Total topics to be removed: 2""".stripMargin,
      shell.executeCommand("remove-all-topics --dry-run").getResult
    )

    assertEquals(
      """|  Topic Name  Enqueued  Dequeued
         |  ----------  --------  --------
         |  testTopic   0         0
         |
         |Total topics: 1""".stripMargin,
      shell.executeCommand("list-topics --filter testTopic").getResult
    )
  }

  @Test
  def testRemoveAllTopicsFilter = {
    assertEquals(info("Topic 'testTopic1' added"), shell.executeCommand("add-topic --name testTopic1").getResult)
    assertEquals(info("Topic 'testTopic2' added"), shell.executeCommand("add-topic --name testTopic2").getResult)
    assertEquals(
      """|Topic removed: 'testTopic2'
         |Total topics removed: 1""".stripMargin,
      shell.executeCommand("remove-all-topics --force --filter 2").getResult
    )

    assertEquals(
      """|  Topic Name  Enqueued  Dequeued
         |  ----------  --------  --------
         |  testTopic1  0         0
         |
         |Total topics: 1""".stripMargin,
      shell.executeCommand("list-topics --filter testTopic").getResult
    )
  }

  @Test
  def testRemoveNonExistingTopic = {
    assertEquals(warn("Topic 'testTopic' does not exist"), shell.executeCommand("remove-topic --name testTopic --force").getResult)
  }

  @Test
  def testAddExistingTopic = {
    assertEquals(info("Topic 'testTopic' added"), shell.executeCommand("add-topic --name testTopic").getResult)
    assertEquals(warn("Topic 'testTopic' already exists"), shell.executeCommand("add-topic --name testTopic").getResult)
  }

  @Test
  def testAvailabilityIndicators: Unit = {
    assertTrue(shell.executeCommand("disconnect").isSuccess)
    try {
      List("list-topics", "add-topic", "purge-topic", "purge-all-topics", "remove-topic", "remove-all-topics").map(command ⇒ {
        assertCommandFailed(shell.executeCommand(command))
      })
    } finally {
      assertTrue(shell.executeCommand("connect --broker test").isSuccess)
    }
  }

}

object TopicCommandsTests {

  val shell = createShell

  @BeforeClass
  def beforeClass() = {
    connectToTestBroker(shell)
  }
}
