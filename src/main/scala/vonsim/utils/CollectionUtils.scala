package vonsim.utils

object CollectionUtils {
  
  implicit class EitherList[A,B,C](a:List[Either[A,B]]){
    
      def mapRight(f:B => C):List[Either[A,C]]={
            
        a.map(x => 
            if (x.isLeft){ 
              Left(x.left.get)
            }else{ 
              Right( f.apply(x.right.get) )
            }
        )
        
      }
      
      def lefts():List[A]={
        a.filter(_.isLeft).map(_.left.get)
      }
      def rights():List[B]={
        a.filter(_.isRight).map(_.right.get)
      }
  }
  
}