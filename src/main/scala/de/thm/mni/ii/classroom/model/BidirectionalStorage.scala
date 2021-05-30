package de.thm.mni.ii.classroom.model

import java.util
import scala.jdk.javaapi.CollectionConverters.asJava

/**
  * Allows access to a by b and b by a
 *
  * @tparam A The type of a
  * @tparam B The type of b
  */
class BidirectionalStorage[A, B] extends ObjectStorage[(A, B)] {
  super.addIndex("_1")
  super.addIndex("_2")

  /**
    * Creats a new BidirectionalMapping
    * @param a the first component
    * @param b the second component
    */
  def put(a: A, b: B): Unit = super.add((a, b))

  /**
    * Gets a by b
    * @param b the given component b
    * @return the returned component a
    */
  def getA(b: B): Set[A] = super.getWhere("_2", b).map(_._1)

  /**
    * Gets b by a
    * @param a the given component a
    * @return the returned component b
    */
  def getB(a: A): Set[B] = super.getWhere("_1", a).map(_._2)

  /**
    * Get all as in the Mapping
    * @return all a
    */
  def getAllA: Set[A] = super.getAll.map(_._1)

  /**
    * Get all bs in the Mapping
    * @return all b
    */
  def getAllB: Set[B] = super.getAll.map(_._2)

  /**
    * Deletes by a
    * @param a the given component a
    * @return the bs of the deleted mapping or empty
    */
  def deleteByA(a: A): Set[B] = {
    val bs = this.getB(a)
    bs.foreach(b => super.remove((a, b)))
    bs
  }

  /**
    * Deletes by b
    * @param b the given component b
    * @return the as of the deleted mapping or empty
    */
  def deleteByB(b: B): Set[A] = {
    val as = this.getA(b)
    as.foreach(a => super.remove((a, b)))
    as
  }

}

/**
 * Wrapper for BidirectionalStorage with Java Collections
 * @tparam A Type A
 * @tparam B Type B
 */
class BidirectionalStorageJava[A, B] {

  private val storage: BidirectionalStorage[A, B] = new BidirectionalStorage[A, B]()

  /**
   * Creates a new BidirectionalMapping
   * @param a the first component
   * @param b the second component
   */
  def put(a: A, b: B): Unit = storage.add((a, b))

  /**
   * Gets a by b
   * @param b the given component b
   * @return the returned component a
   */
  def getA(b: B): util.Set[A] = asJava(storage.getA(b))

  /**
   * Gets b by a
   * @param a the given component a
   * @return the returned component b
   */
  def getB(a: A): util.Set[B] = asJava(storage.getB(a))

  /**
   * Get all as in the Mapping
   * @return all a
   */
  def getAllA: util.Set[A] = asJava(storage.getAllA)

  /**
   * Get all bs in the Mapping
   * @return all b
   */
  def getAllB: util.Set[B] = asJava(storage.getAllB)

  /**
   * Deletes by a
   * @param a the given component a
   * @return the bs of the deleted mapping or empty
   */
  def deleteByA(a: A): util.Set[B] = asJava(storage.deleteByA(a))

  /**
   * Deletes by b
   * @param b the given component b
   * @return the as of the deleted mapping or empty
   */
  def deleteByB(b: B): util.Set[A] = asJava(storage.deleteByB(b))

}
