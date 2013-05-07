#-*- coding: UTF-8 -*- 
import MapUtil as MU
import math
import xml.etree.ElementTree as ET
EarthR=63781370.0
idCurrent=2

class IDGenerator(object):
    def __init__(self,beginID):
        self.idCurrent=int(beginID)
    def setBeginID(self,beginID):
        self.idCurrent=int(beginID)
    def genID(self):
        self.idCurrent+=1
        return self.idCurrent
class Block(object):
    'Map Block'
    'xid'
    'yid'
    'id'
    'level'
    'Way List Roads'
    def __init__(self):
        self.id=0L
        self.level=32
        self.xid=0
        self.yid=0
        self.roads=[]
    def initwithID(self,blockid,level):
        self.id=blockid
        self.level=level
        xyid=Block.calculateXYid(blockid, level)
        self.xid=xyid[0]
        self.yid=xyid[1]
        self.pos=MU.Node(self.getPosbySideID(self.xid),self.getPosbySideID(self.yid))
    def initwithXYID(self,xid,yid,level):
        self.xid=xid
        self.yid=yid
        self.level=level
        self.id=Block.calcultateID(xid, yid, level)
        self.pos=MU.Node(self.getPosbySideID(self.xid),self.getPosbySideID(self.yid))
    @staticmethod
    def calculateXYID(blockid,level):
        j=0x0000000000000001L
        l=32
        xid=0
        yid=0
        if(level>l):
            level=l
        for i in range(0,level*2,1):
            if i&0x00000001==0:
                xid+=(blockid&j)>>(i/2)
            else:
                yid+=(blockid&j)>>((i+1)/2)
            j<<=1
        return (xid,yid)
    def getPosbySideID(self,sideid):
        return math.pi*EarthR*(float(sideid)/float(1<<(self.level-1))-1)
    @staticmethod
    def calcultateID(xid,yid,level):
        j=0x0000000000000001L
        l=32
        id=0
        if(level>l):
            level=l
        for i in range(0,level,1):
            id+=(xid&j)<<i
            id+=(yid&j)<<(i+1)
            j<<=1
        return id
    def addroad(self,way):
        self.roads.append(way)
    def WriteToBlockFile(self,filepath,idgen):
        try:
            doc=ET.parse(filepath)
        except IOError:
            doc=ET.ElementTree()
            root=ET.Element("block",{"id":str(self.id),"level":str(self.level)})
            doc._setroot(root)
        root=doc.getroot()
        for i in self.roads:
            road=ET.Element("road",{"name":str(i.attributes["name"]),"id":str(idgen.genID())})
            iter=root.iter(None)
            has=False
            for element in iter:
                if  road.get("id", None)==element.get("id"):
                    has=True
                    break
            if has==True:
                continue
            triangles=ET.Element("triangles",{"type":"strip","material":"road"})
            road.append(triangles)
            for nd in i.pointlist:
                nd=ET.Element("node",{"x":str(nd.x-self.pos.x),"y":str(0),"z":str(nd.y-self.pos.y)})
                triangles.append(nd)
            root.append(road)
        children=list(root)
        if(len(children)>0):
            doc.write(filepath, "UTF-8", "<?xml version='1.0' encoding='UTF-8'?>", None, "xml")
            
def UTMtoOSM(src,dst):
    doc=ET.parse(src)
    #get root element <osm>
    root=doc.getroot()
    nodes=list(root.iterfind("node"))
    for i in nodes:
        pos=MU.Node(float(i.attrib.pop('lon')),float(i.attrib.pop('lat')))
        MU.UTMToLonLat(pos)
        i.attrib["lon"]=str(pos.x)
        i.attrib["lat"]=str(pos.y)
    doc.write(dst, "UTF-8", "<?xml version='1.0' encoding='UTF-8'?>", None,"xml" )

def OSMtoUTM2D(src,dst):
    doc=ET.parse(src)
    #get root element <osm>
    root=doc.getroot()
    nodes=list(root.iterfind("node"))
    for i in nodes:
        pos=MU.Node(float(i.attrib.pop('lon')),float(i.attrib.pop('lat')))
        MU.LonLatToUTM(pos)
        i.attrib["x"]=str(pos.x)
        i.attrib["y"]=str(pos.y)
    doc.write(dst, "UTF-8", "<?xml version='1.0' encoding='UTF-8'?>", None,"xml" )

class Ways(object):
    'ElementTree doc'
    'Node Dict points'
    'Way List waylist'
    'AABB2D boundingBox'
    'Relation List splittedWays'
    'Relation List triWays'
    idCurrent=0
    def __init__(self,filepath):
        self.waylist=[]
        self.points={}
        self.doc=ET.parse(filepath)
        root=self.doc.getroot()
        
        minx,miny,maxx,maxy=MU.AABB2D.worldMaxX,MU.AABB2D.wordMaxY,MU.AABB2D.worldMinX,MU.AABB2D.worldMinY
        
        nodes=list(root.iterfind("node"))
        for i in nodes:
            node=MU.Node(i)
            if node.x>maxx:
                maxx=node.x
            if node.x<minx:
                minx=node.x
            if node.y>maxy:
                maxy=node.y
            if node.y<miny:
                miny=node.y
            self.points[node.id]=node
        self.boundingBox=MU.AABB2D(minx,miny,maxx,maxy)
        waytree=list(root.iterfind("way"))
        for i in waytree:
            way=MU.Road()
            way.initwithXmlNode(i, self.points)
            self.waylist.append(way)
    def calculateBlocks(self,level):
        self.worldsideNum=1<<level
        self.blocklength=2*MU.AABB2D.worldMaxX/self.worldsideNum
        print '层级',level
        print '块边长',str(self.blocklength)
        self.minXindex=self.getsideIndex(self.boundingBox.minx)
        self.minYindex=self.getsideIndex(self.boundingBox.miny)
        self.maxXindex=self.getsideIndex(self.boundingBox.maxx)
        self.maxYindex=self.getsideIndex(self.boundingBox.maxy)
        self.blocks={}
        for i in range(self.minXindex,self.maxXindex+1):
            for j in range(self.minYindex,self.maxYindex+1):
                block=Block()
                block.initwithXYID(i, j, level)
                self.blocks[(i,j)]=block
    def calBlockLines(self):
        idgen=IDGenerator(6000)
        for i in range(self.minXindex,self.maxXindex+2,1):
            blocklines=MU.Relation()
            self.splittedWays.append(blocklines)
            blockline=MU.Road()
            blockline.initWithID(idgen)
            x=self.getxybysideIndex(i)
            for j in range(self.minYindex,self.maxYindex+2,1):
                y=self.getxybysideIndex(j)
                Nd=MU.Node(x, y, idgen.genID())
                blockline.append(Nd)
            blocklines.append(blockline)
        for j in range(self.minYindex,self.maxYindex+2,1):
            blocklines=MU.Relation()
            self.splittedWays.append(blocklines)
            blockline=MU.Road()
            blockline.initWithID(idgen)
            y=self.getxybysideIndex(j)
            for i in range(self.minXindex,self.maxXindex+2,1):
                x=self.getxybysideIndex(i)
                Nd=MU.Node(x, y, idgen.genID())
                blockline.append(Nd)
            blocklines.append(blockline)
        
    def getsideIndex(self,x):
        return int(x/self.blocklength+self.worldsideNum/2)
    def getxybysideIndex(self,index):
        return index*self.blocklength-math.pi*EarthR
    def SplitWayintoBlocks(self,level):
        idgen=IDGenerator(1)
        self.splittedWays=[]
        for i in self.waylist:
            x0=self.getsideIndex(i.pointlist[0].x)
            y0=self.getsideIndex(i.pointlist[0].y)
            x1=0
            y1=0
            iffirstLine=1
            p0=i.pointlist[0]
            # 新建一条子路
            subway=MU.Road()
            #初始化子路
            subway.initWithID(idgen)
            #新建一条路关系
            newrelation=MU.Relation()
            #把该路关系加入路列表
            self.splittedWays.append(newrelation)
            #把子路加入到路关系
            newrelation.append(subway)
            newrelation.attributes=i.attributes.copy()
            #对于每对顶点
            for p1 in i.pointlist[1:]:
                x1=self.getsideIndex(p1.x)
                y1=self.getsideIndex(p1.y)
                splitXNode=[]
                splitYNode=[]
                #如果0顶点的块xid和1的xid相等
                #求交点
                if x0==x1:
                    pass
                elif x0<x1:
                    for k in range(x0+1,x1+1,1):
                        crossx=self.getxybysideIndex(k)
                        crossy=(p1.y-p0.y)*(crossx-p0.x)/(p1.x-p0.x)+p0.y
                        crossnode=MU.Node(crossx,crossy,idgen.genID())
                        crossnode.redundancy=0
                        splitXNode.append(crossnode)
                else:
                    for k in range(x0,x1,-1):
                        crossx=self.getxybysideIndex(k)
                        crossy=(p1.y-p0.y)*(crossx-p0.x)/(p1.x-p0.x)+p0.y
                        crossnode=MU.Node(crossx,crossy,idgen.genID())
                        crossnode.redundancy=0
                        splitXNode.append(crossnode)
                if y0==y1:
                    pass
                elif y0<y1:
                    for k in range(y0+1,y1+1,1):
                        
                        crossy=self.getxybysideIndex(k)
                        crossx=(p1.x-p0.x)*(crossy-p0.y)/(p1.y-p0.y)+p0.x
                        crossnode=MU.Node(crossx,crossy,idgen.genID())
                        crossnode.redundancy=0
                        splitYNode.append(crossnode)
                else:
                    for k in range(y0,y1,-1):
                        crossy=self.getxybysideIndex(k)
                        crossx=(p1.x-p0.x)*(crossy-p0.y)/(p1.y-p0.y)+p0.x
                        crossnode=MU.Node(crossx,crossy,idgen.genID())
                        crossnode.redundancy=0
                        splitYNode.append(crossnode)
                        
                #去重
                if len(splitXNode)!=0:
                    if len(splitYNode)!=0:
                        #去除与AB重复的点
                        if MU.Node.ifSamePos(splitXNode[0], p0, 0.001):
                            splitXNode[0].redundancy=1
                        if MU.Node.ifSamePos(splitXNode[-1], p1, 0.001):
                            splitXNode[-1].redundancy=1
                        if MU.Node.ifSamePos(splitYNode[0], p0, 0.001):
                            splitYNode[0].redundancy=1
                        if MU.Node.ifSamePos(splitYNode[-1], p1, 0.001):
                            splitYNode[-1].redundancy=1
                        #去除相互重复的点
                        for xnd in splitXNode:
                            for ynd in splitYNode:
                                if MU.Node.ifSamePos(xnd, ynd, 0.01):
                                    ynd.redundancy=1
                        
                        
                    else:
                        #去除与AB重复的点
                        if MU.Node.ifSamePos(splitXNode[0], p0, 0.01):
                            splitXNode[0].redundancy=1
                        if MU.Node.ifSamePos(splitXNode[-1], p1, 0.01):
                            splitXNode[-1].redundancy=1
                else:
                    if len(splitYNode)!=0:
                        #去除与AB重复的点
                        if MU.Node.ifSamePos(splitYNode[0], p0, 0.01):
                            splitYNode[0].redundancy=1
                        if MU.Node.ifSamePos(splitYNode[-1], p1, 0.01):
                            splitYNode[-1].redundancy=1
                    else:
                        pass
                for xnd in splitXNode:
                    if xnd.redundancy==1:
                        print "redundancy"
                        splitXNode.remove(xnd)
                for ynd in splitYNode:
                    if ynd.redundancy==1:
                        print "redundancy"
                        splitYNode.remove(ynd)
                #对去重后的顶点排序
                nd0=MU.Node(p0.x,p0.y,idgen.genID())
                ndlist=[]
                ndlist.append(nd0)
                ndlist+=splitXNode+splitYNode
                nd1=MU.Node(p1.x, p1.y, idgen.genID())
                ndlist.append(nd1)
                ndlist=sorted(ndlist,key=lambda i:(i.x-ndlist[0].x)*(i.x-ndlist[0].x)+(i.y-ndlist[0].y)*(i.y-ndlist[0].y))
                #写入数据结构
                if iffirstLine:
                    subway.append(ndlist[0])
                    subway.append(ndlist[1])
                    subway.attributes["blockid"]=Block.calcultateID(self.getsideIndex((ndlist[0].x+ndlist[1].x)/2.0),self.getsideIndex((ndlist[0].y+ndlist[1].y)/2.0),level)
                    if(len(ndlist)>2):
                        for nd in range(2,len(ndlist),1):
                            newway=MU.Road()
                            newway.attributes["blockid"]=Block.calcultateID(self.getsideIndex((ndlist[nd-1].x+ndlist[nd].x)/2.0),self.getsideIndex((ndlist[nd-1].y+ndlist[nd].y)/2.0),level)
                            newway.initWithID(idgen)
                            newway.append(ndlist[nd-1])
                            newway.append(ndlist[nd])
                            newrelation.append(newway)
                            subway=newway
                    iffirstLine=0
                else:
                    for nd in range(1,len(ndlist),1):
                        ndBlockid=Block.calcultateID(self.getsideIndex((ndlist[nd-1].x+ndlist[nd].x)/2.0),self.getsideIndex((ndlist[nd-1].y+ndlist[nd].y)/2.0),level)
                        if(ndBlockid!=subway.attributes["blockid"]):
                            newway=MU.Road()
                            newway.initWithID(idgen)
                            newway.attributes["blockid"]=ndBlockid
                            newway.append(ndlist[nd-1])
                            newway.append(ndlist[nd])
                            newrelation.append(newway)
                            subway=newway
                        else:
                            subway.append(ndlist[nd])
                p0=p1
                x0=x1
                y0=y1
    def writeSplitedWaytoFile(self):
        #import xml.etree.ElementTree as ET
        doc=ET.ElementTree()
        root=ET.Element("osm",{'generator':'JOSM','upload':'true','version' :'0.6' })
        doc._setroot(root)
        root.text=='\n'
        for i in self.splittedWays:
            for j in i.children:
                for k in j.pointlist:
                    nodeattribute={"action":"modify","id":str(-1*k.id), "visible":"true", "lon":str(k.x), "lat":str(k.y)}
                    node=ET.Element('node',nodeattribute)
                    node.tail="\n\t"
                    root.append(node)
        for i in self.splittedWays:
            for j in i.children:
                wayattribute={"action":"modify","id":str(-1*j.id),"visible":"true"}
                way=ET.Element("way",wayattribute)
                for k in j.pointlist:
                    ndattribute={"ref":str(-1*k.id)}
                    nd=ET.Element('nd',ndattribute)
                    nd.tail="\n\t\t"
                    way.append(nd)
                way.tail="\n\t"
                way.text="\n\t\t"
                root.append(way)
        doc=ET.ElementTree()
        doc._setroot(root)
        doc.write("d:/e.osm", "UTF-8", "<?xml version='1.0' encoding='UTF-8'?>", None, 'xml')
    def translateWaysToTriangles(self,laneWidth):
        'laneWidth 车道宽度 单位分米'
        def getNext(splittedWay):
            try:
                if(splittedWay.subwaycount==-2):
                    return None
                splittedWay.subwaycount+=1
                
            except AttributeError:
                splittedWay.subwaycount=0
                splittedWay.waycount=0
                B=MU.Node(splittedWay.children[0].pointlist[0].x,splittedWay.children[0].pointlist[0].y)
                BC=MU.Node(splittedWay.children[0].pointlist[1].x-splittedWay.children[0].pointlist[0].x,splittedWay.children[0].pointlist[1].y-splittedWay.children[0].pointlist[0].y)
                BA=MU.Node()
                if math.fabs(splittedWay.children[0].pointlist[0].x-splittedWay.children[-1].pointlist[-1].x)<0.001 and\
                math.fabs(splittedWay.children[0].pointlist[0].y-splittedWay.children[-1].pointlist[-1].y)<0.001:
                    splittedWay.round=1
                    BA.x=splittedWay.children[-1].pointlist[-2].x-splittedWay.children[-1].pointlist[-1].x
                    BA.y=splittedWay.children[-1].pointlist[-2].y-splittedWay.children[-1].pointlist[-1].y
                else:
                    splittedWay.round=0
                    BA.x=0.0
                    BA.y=0.0
                return (BA,BC,B)
            #到子路的最后一项了   
            if(splittedWay.subwaycount==len(splittedWay.children[splittedWay.waycount].pointlist)-1):
                BA=MU.Node()
                BA.x=splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount-1].x-\
                splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount].x
                BA.y=splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount-1].y-\
                splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount].y
                B=MU.Node()
                B.x=splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount].x
                B.y=splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount].y
                #到最后一个子路了
                if splittedWay.waycount==len(splittedWay.children)-1:
                    splittedWay.subwaycount=-2
                    if splittedWay.round==1:
                        BC=MU.Node()
                        BC.x=splittedWay.children[0].pointlist[0].x-\
                            splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount].x
                        BC.y=splittedWay.children[0].pointlist[0].y-\
                            splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount].y
                        return (BA,BC,B)
                    else:
                        BC=MU.Node()
                        BC.x=0.0
                        BC.y=0.0
                        return (BA,BC,B)
                #没到最后一个子路
                else:
                    BC=MU.Node()
                    BC.x=splittedWay.children[splittedWay.waycount+1].pointlist[1].x-\
                    splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount].x
                    BC.y=splittedWay.children[splittedWay.waycount+1].pointlist[1].y-\
                    splittedWay.children[splittedWay.waycount].pointlist[splittedWay.subwaycount].y
                    splittedWay.waycount+=1
                    splittedWay.subwaycount=0
                    return (BA,BC,B)
            #中间情况
            else:
                tmp=splittedWay.children[splittedWay.waycount].pointlist
                B=MU.Node()
                B.x=tmp[splittedWay.subwaycount].x
                B.y=tmp[splittedWay.subwaycount].y
                BA=MU.Node()
                BA.x=tmp[splittedWay.subwaycount-1].x-tmp[splittedWay.subwaycount].x
                BA.y=tmp[splittedWay.subwaycount-1].y-tmp[splittedWay.subwaycount].y
                BC=MU.Node()
                BC.x=tmp[splittedWay.subwaycount+1].x-tmp[splittedWay.subwaycount].x
                BC.y=tmp[splittedWay.subwaycount+1].y-tmp[splittedWay.subwaycount].y
                return (BA,BC,B)  
        
        self.triWays=[]
        idgen=IDGenerator(2)
        for i in self.splittedWays:
            width=float(i.attributes["width"])*float(laneWidth)*0.5
            triStripWay=MU.Relation()
            triStripWay.attributes=i.attributes.copy()
            self.triWays.append(triStripWay)
            i.waycount=0
            lastwaycount=0
            subTri=MU.Road()
            subTri.initWithID(idgen)
            subTri.attributes=i.children[i.waycount].attributes.copy()
            while True:
                lastwaycount=i.waycount
                n=getNext(i)
                if(n==None):
                    break
                if(n[1].x!=0.0 or n[1].y!=0.0):
                    if(n[0].x!=0.0 or n[0].y!=0.0):
                        
                        n[0].normalize()
                        n[1].normalize()
                        a=n[0]+n[1]
                        try:
                            a.normalize()
                            cosa=a.x*n[0].y*-1+a.y*n[0].x
                            r=width/cosa
                            n[0].x=a.x*r
                            n[0].y=a.y*r
                            n[1].x=a.x*r*-1
                            n[1].y=a.y*r*-1
                            n[0].add(n[2])
                            n[1].add(n[2])
                        except ZeroDivisionError:
                            tmp=n[1].x
                            n[1].x=-1*n[1].y
                            n[1].y=tmp
                            n[0].x=-1*n[1].x
                            n[0].y=-1*n[1].y
                            n[0].add(n[2])
                            n[1].add(n[2])
                        
                    else:
                        n[1].normalize()
                        tmp=n[1].x*width
                        n[1].x=-1*n[1].y*width
                        n[1].y=tmp
                        n[0].x=-1*n[1].x
                        n[0].y=-1*n[1].y
                        n[0].add(n[2])
                        n[1].add(n[2])
                else:
                    n[0].normalize()
                    tmp=n[0].x*width
                    n[0].x=-1*n[0].y*width
                    n[0].y=tmp
                    n[1].x=-1*n[0].x
                    n[1].y=-1*n[0].y
                    n[1].add(n[2])
                    n[0].add(n[2])
                n0=MU.Node(n[0].x,n[0].y , idgen.genID())
                n1=MU.Node(n[1].x, n[1].y, idgen.genID())
                #如果这个当前求的点是子路的最后一个点
                #则把它作为当前子路的最后节点和后一个子路的第一个节点
                if lastwaycount!=i.waycount:
                    subTri.append(n0)
                    subTri.append(n1)
                    triStripWay.append(subTri)
                    subTri=MU.Road()
                    subTri.append(n0)
                    subTri.append(n1)
                    subTri.attributes=i.children[i.waycount].attributes.copy()
                    subTri.initWithID(idgen)
                else:
                    subTri.append(n0)
                    subTri.append(n1)
            triStripWay.append(subTri)
    def writeSplittedTriToFile(self):
        doc=ET.ElementTree()
        root=ET.Element("osm",{'generator':'JOSM','upload':'true','version' :'0.6' })
        doc._setroot(root)
        root.text=='\n'
        for i in self.triWays:
            for j in i.children:
                for k in j.pointlist:
                    nodeattribute={"action":"modify","id":str(-1*k.id), "visible":"true", "lon":str(k.x), "lat":str(k.y)}
                    node=ET.Element('node',nodeattribute)
                    node.tail="\n\t"
                    root.append(node)
        for i in self.triWays:
            for j in i.children:
                wayattribute={"action":"modify","id":str(-1*j.id),"visible":"true"}
                way=ET.Element("way",wayattribute)
                for k in j.pointlist:
                    ndattribute={"ref":str(-1*k.id)}
                    nd=ET.Element('nd',ndattribute)
                    nd.tail="\n\t\t"
                    way.append(nd)
                way.tail="\n\t"
                way.text="\n\t\t"
                root.append(way)
        doc=ET.ElementTree()
        doc._setroot(root)
        doc.write("d:/c.osm", "UTF-8", "<?xml version='1.0' encoding='UTF-8'?>", None, 'xml')
    
    def trisToBlocks(self,level):
        for i in self.triWays:
            for j in i.children:
                j.attributes.update(i.attributes)
                xyid=Block.calculateXYID(int(j.attributes["blockid"]),level)
                self.blocks[xyid].addroad(j)
    
    def WriteWaysToBlockFile(self):
        path="d:/blocks/"
        idgen=IDGenerator(2) 
        iter=self.blocks.itervalues()
        for i in iter:
            file=path+str(i.id)+".xml"
            i.WriteToBlockFile(file,idgen)


OSMtoUTM2D("d:/a.osm","d:/b.osm")
map=Ways("d:/b.osm")
map.calculateBlocks(17)
map.SplitWayintoBlocks(17)
#map.calBlockLines()
map.writeSplitedWaytoFile()
map.translateWaysToTriangles(45)
map.trisToBlocks(17)
map.WriteWaysToBlockFile()
map.writeSplittedTriToFile()
UTMtoOSM("d:/e.osm","d:/f.osm")
UTMtoOSM("d:/c.osm","d:/d.osm")


'''
a=Block.calculateXYID(0x155555555L, 17)
print hex(a[0])
print a[1]
print hex(Block.calcultateID( 0x1ffff,0, 17))
'''
#for i in map.waylist:
#    print i.attributes["name"]