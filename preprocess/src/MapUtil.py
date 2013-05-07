# -*- coding: UTF-8 -*- 
import math
EarthR = 63781370.0
def LonLatToUTM(pos):
    pos.x = EarthR * math.pi * pos.x / 180.0;
    pos.y = EarthR * math.log(math.tan((90 + pos.y) * math.pi / 360.0));
def UTMToLonLat(pos):
    pos.x = pos.x * 180 / (math.pi * EarthR)
    pos.y = 360.0 * math.atan(math.exp(pos.y / EarthR)) / math.pi - 90

def det(m):
    size = len(m)
    assert(size == len(m[0]))
    if size == 1:
        return m[0][0]
    res = 0.0
    mul = 1.0
    def m_xy(row, col):
        m_xy = []
        for eachrow in m:
            m_xy.append(eachrow[:])
        m_xy.pop(row)
        for eachrow in m_xy:
            eachrow.pop(col)
        return m_xy
    for col in range(size):
        res += mul * m[0][col] * det(m_xy(0, col))
        mul = -mul
    return res

class AABB2D(object):
    worldMaxX = math.pi * EarthR
    wordMaxY = worldMaxX
    worldMinX = -worldMaxX
    worldMinY = worldMinX
    def __init__(self, minix, miniy, maxx, maxy):
        self.minx = float(minix)
        self.miny = float(miniy)
        self.maxx = float(maxx)
        self.maxy = float(maxy)

class Way(object):
    'Node* pointlist'
    def __init__(self):
        self.pointlist = []
        self.attributes = {}
    def append(self, NewNode):
        # if(len(self.pointlist)!=0):
            # self.pointlist[len(self.pointlist)-1].setrefWay(self,1)
        self.pointlist.append(NewNode)
        # self.pointlist[len(self.pointlist)-1].setrefWay(self,1)
    def __len__(self):
        return len(self.pointlist)
    def __getitem__(self, ind):
        return self.pointlist[ind]
    def __setitem__(self, ind, val):
        self.pointlist[ind] = val
    def __delitem__(self, ind):
        del self.pointlist[ind]

class Road(Way):
    def initwithXmlNode(self, subElement, nodes):
        ndrefs = list(subElement.iterfind("nd"))
        j = 0
        size = len(ndrefs)
        for i in ndrefs:
            refnd = nodes[int(i.attrib["ref"])]
            self.pointlist.append(refnd)
            '''if (j==0)or(j==size-1):
                refnd.setrefWay(self,1)
            else:
                refnd.setrefWay(self, 2)'''
        attribs = list(subElement.iterfind("tag"))
        self.attributes = {}
        for i in attribs:
            self.attributes[i.attrib["k"]] = i.attrib["v"]
    def initWithID(self, idgen):
        self.id = idgen.genID()

class SimplePolygon(Way):
    def initWithXmlNode(self, wayNode, nodes):
        pass

class Node(object):
    def __init__(self, *vector):
        if len(vector) == 2:
            self.x = float(vector[0])
            self.y = float(vector[1])
        elif len(vector) == 3:
            self.x = float(vector[0])
            self.y = float(vector[1])
            self.id = int(vector[2])
        elif len(vector) == 1:
            self.x = float(vector[0].attrib["x"])
            self.y = float(vector[0].attrib["y"])
            self.id = int(vector[0].attrib["id"])
        else:
            self.x = 0.0
            self.y = 0.0
        # self.refways=[]
        # self.refcount=0
        self.iscross = False
    def add(self, obj):
        self.x += obj.x
        self.y += obj.y
    def __add__(self, obj):
        return Node(self.x + obj.x, self.y + obj.y)
    def __radd__(self, obj):
        return Node(self.x + obj.x, self.y + obj.y)
    def __iadd__(self, obj):
        self.x -= obj.x
        self.y -= obj.y
        return self
    def __sub__(self, obj):
        return Node(self.x - obj.x, self.y - obj.y)
    def __rsub__(self, obj):
        return Node(obj.x - self.x, obj.y - self.y)
    def __isub__(self, obj):
        self.x -= obj.x
        self.y -= obj.y
        return self
    def __str__(self):
        return "x=" + str(self.x) + "  y=" + str(self.y)
    def __repr__(self):
        return "x=" + str(self.x) + "  y=" + str(self.y)
    def __mul__(self, obj):
        return self.x * obj.x + self.y * obj.y
    def normalize(self):
        length = 1.0 / math.sqrt(self * self)
        assert(length != 0)
        self.x *= length
        self.y *= length
    def __pow__(self, other):
        return self.x * other.y - self.y * other.x
    '''
    def setrefWay(self,way,refcount):
        if self.refways.count(way)==0:
            self.refways.append(way)
        self.refcount+=refcount
        '''
    @staticmethod
    def ifSamePos(nd1, nd2, epsion):
        x = nd2.x - nd1.x
        y = nd2.y - nd1.y
        if(x * x + y * y < epsion * epsion):
            return 1
        else:
            return 0

class Relation(object):
    'self.children'
    'dict self.attributes'
    def __init__(self):
        self.children = []
        self.attributes = {}
    def append(self, object):
        self.children.append(object)
    def __len__(self):
        return len(self.children)
    def __getitem__(self, ind):
        return self.children[ind]
    def __setitem__(self, ind, val):
        self.chidren[ind] = val
    def __delitem__(self, ind):
        del self.children[ind]

class Line(object):
    eps = 0.0000001
    def __init__(self, a, b):
        self.A = a
        self.B = b
        self.AB = Node(b.x - a.x, b.y - a.y)
        self.c = a.x * b.y - a.y * b.x
    def IntersectionWithpoint(self, p):
        'return 0 if point on line'
        'return -1 if right'
        'return 1 if left'
        apx = p.x - self.A.x
        apy = p.y - self.A.y
        r = self.AB.x * apy - self.AB.y * apx
        if r > Line.eps:
            return 1
        elif r < -Line.eps:
            return -1
        else:
            return 0

    def LineSegmentWithPoint(self, p):
        'regard self as a line segment'
        'return 1 if point on left'
        'return -1 if point on right'
        'return 0 if point on line segment'
        'return 2 if point on line but out of line segment bound'
        r = self.IntersectionWithpoint(p)
        if r == 0:
            minx = self.A.x if self.A.x < self.B.x else self.B.x
            maxx = self.A.x if self.A.x > self.B.x else self.B.x
            miny = self.A.y if self.A.y < self.B.y else self.B.y
            maxy = self.A.y if self.A.y > self.B.y else self.B.y
            if p.x >= minx and p.x <= maxx and p.y >= miny and p.y <= maxy:
                return 0
            else:
                return 2
        return r

    def RayWithLineSegment(self, A, B):
        'regard self as a Ray ,self.A is the origin point'
        'A,B is the point of the line segment'
        r = self.IntersectionWithLineSegment(A, B)
        if abs(r[0]) <= 1:
            s = (r[1].x - self.A.x) * self.AB.x + (r[1].y - self.A.y) * self.AB.y
            if s < 0:
                return (r[0] * 2, None)
            else:return r
        else:
            return r
    def IntersectionWithLineSegment(self, A, B):
        'return (type,crossPoint)'
        'type 0:segment cross line'
        'type 1:segment on left but A or B on line'
        'type -1:segment on right,but A or B on line'
        'type 2:segment on left'
        'type -2:segment on right'
        'type 3:segment on line'
        ares = self.IntersectionWithpoint(A)
        bres = self.IntersectionWithpoint(B)
        if ares * bres > 0:
            if(ares > 0):
                return (2, None)
            else:
                return (-2, None)
        elif ares * bres < 0:
            l2A = A.y - B.y
            l2B = A.x - B.x
            l2C = A.x * B.y - A.y * B.x
            crossPoint = Node()
            D = 1.0 / (self.AB.y * l2B - l2A * self.AB.x)
            crossPoint.x = (self.AB.x * l2C + l2B * self.c) * D
            crossPoint.y = (l2A * self.c + self.AB.y * l2C) * D
            if(ares < 0):
                return (0, crossPoint)
            else:
                return (0, crossPoint)
        elif bres != 0.0:
            if(bres > 0):
                return (1, A)
            else:
                return (-1, A)
        elif ares != 0.0:
            if(ares > 0):
                return (1, B)
            else:
                return (-1, B)
        else:
            return (3, None)
    

class MultiPolygon(Relation):
    def __init__(self, *rings):
        super(MultiPolygon, self).__init__()
        self.children = list(rings)
    def edges(self):
        edges = []
        for l in self.children:
            edges += [(l[i - 1], l[i]) for i in range(len(l))]
        return edges
    def edge_num(self):
        return sum([len(i) for i in self.children])
    def on_line_left(self, p, line):
        return (line[1] - line[0]) ** (p - line[1]) > Line.eps
    def is_intersect(self, line1, line2):
        return (line1[1] - line1[0]) ** (line2[0] - line1[1]) * (line1[1] - line1[0]) ** (line2[1] - line1[1]) <= Line.eps and \
        (line2[0] - line2[1]) ** (line1[0] - line2[0]) * (line2[0] - line2[1]) ** (line1[1] - line2[0]) <= Line.eps
    def p_in_tri_circle(self, d, a, b, c):
        row = lambda p:[p.x, p.y, p.x ** 2 + p.y ** 2, 1]
        m = [row(a), row(b), row(c), row(d)]
        return det(m) > Line.eps    
    def Clipping(self, line):
        rightlist = []
        leftlist = []
        rightinners = []
        leftinners = []
        for list in self.children:
            right, left, n = [], [], 0
            lastSide = line.IntersectionWithpoint(list[0])
            while(lastSide == 0):
                n += 1
                lastSide = line.IntersectionWithpoint(list[n])
            for k in range(n + 1, len(list) + n + 1, 1):
                i = k % len(list)
                result = line.IntersectionWithLineSegment(list[i - 1], list[i])
                if(result[0] == 0):
                    left.append(result[1])
                    right.append(result[1])
                    result[1].iscross = True
                    (left if lastSide < 0 else right).append(list[i])
                    lastSide *= -1
                elif result[0] == 2 :left.append(list[i])
                elif result[0] == -2 : right.append(list[i])
                elif result[0] == 1:
                    result[1].iscross = True
                    lastSide = 1
                    if result[1] == list[i]:left.append(list[i - 1])
                    left.append(list[i])
                elif result[0] == -1:
                    result[1].iscross = True
                    if result[1] == list[i]:right.append(list[i - 1])
                    right.append(list[i])
                    lastSide = -1
                else:pass
            if len(right) > 0:
                if not (right[0].iscross and right[-1].iscross):
                    for i in range(0, len(right), 1):
                        if right[i].iscross:
                            right = right[i + 1:len(right):1] + right[0:i + 1]
                            break
                if right[0].iscross:
                    p = 0
                    for i in range(1, len(right), 1):
                        if right[i].iscross:
                            if i - p > 1:
                                sub = right[p:i + 1]
                                rightlist.append(sub)
                            p = i
                else:           
                    rightinners.append(right)
                    if list == self.children[0]:
                        return ([self], [])
            if len(left) > 0:
                if not (left[0].iscross and left[-1].iscross):
                    for i in range(0, len(left), 1):
                        if left[i].iscross:
                            left = left[i + 1:len(left):1] + left[0:i + 1]
                            break
                if left[0].iscross:
                    p = 0
                    for i in range(1, len(left), 1):
                        if left[i].iscross:
                            if i - p > 1:
                                sub = left[p:i + 1]
                                leftlist.append(sub)
                            p = i
                else:
                    leftinners.append(left)
                    if list == self.children[0]:
                        return ([], [self])
        def mergeOneSide(onelist, inners, line):
            crossPoints = [x[0] for x in onelist for i in (0, -1)]
            polygons = []
            def findCrossPos(onelist, p):
                a = [x for x in onelist if x[0] == p]
                b = [x for x in onelist if x[-1] == p]
                return a if a else [None] + b if b else [None]
            if len(crossPoints) > 1:
                if abs(line.AB.x) > 0.00001:
                    crossPoints = sorted(crossPoints, key=lambda i:(i.x - crossPoints[0].x) * line.AB.x)
                else:
                    crossPoints = sorted(crossPoints, key=lambda i:(i.y - crossPoints[0].y) * line.AB.y)
            for i in range(0, len(crossPoints), 2):
                A = crossPoints[i]
                B = crossPoints[i + 1]
                ifselfRound = False
                for j in onelist:
                    if (A == j[0] and B == j[-1]) or (A == j[-1] and B == j[0]):
                        polygon = MultiPolygon()
                        polygon.children.append(j)
                        polygons.append(polygon)
                        j[0].iscross = False
                        j[-1].iscross = False
                        onelist.remove(j)
                        ifselfRound = True
                        break
                if not ifselfRound:
                    if A == B:
                        r = findCrossPos(onelist, A)
                        Bpos = r[0]
                        Apos = r[1]
                        Apos[-1].iscross = False
                        onelist.append(Apos + Bpos[1:len(Bpos)])
                        onelist.remove(Apos)
                        onelist.remove(Bpos)
                    else:
                        r1 = findCrossPos(onelist, A)
                        r2 = findCrossPos(onelist, B)
                        if None in r1:
                            if r1[0] == None:
                                Apos = r1[1]
                                Bpos = r2[0]
                            else:
                                Apos = r2[1]
                                Bpos = r1[0]
                        else:
                            if r2[0] == None:
                                Apos = r2[1]
                                Bpos = r1[0]
                            else:
                                Apos = r1[1]
                                Bpos = r2[0]
                        Apos[-1].iscross = False
                        Bpos[0].iscross = False
                        onelist.append(Apos + Bpos)
                        onelist.remove(Apos)
                        onelist.remove(Bpos)
            for i in inners:
                j, k = 0, 0
                while True:
                    r = polygons[k].IntersectionWithpoint(i[j])
                    if r > 0:
                        polygons[k].children.append(i)
                        break
                    elif r < 0:
                        k += 1
                        break
                    else:
                        j += 1
            return polygons
        leftpolygons = mergeOneSide(leftlist, leftinners, line)
        rightpolygons = mergeOneSide(rightlist, rightinners, line)
        return (rightpolygons, leftpolygons)
    def IntersectionWithpoint(self, p):
        for i in self.children:
            for j in range(0, len(i), 1):
                l = Line(i[j], i[(j + 1) % len(i)])
                r = l.LineSegmentWithPoint(p)
                if r == 0:return 0
        points = 0
        line = Line(p, Node(p.x + 100, p.y + 100))
        for i in self.children:
            k = 0
            lastSide = 0
            for j in range(0, len(i), 1):
                r = line.RayWithLineSegment(i[j], i[j + 1])
                if r[0] == 2 or r[0] == -2:
                    k = j
                    break
            for j in range(k, len(i) + k, 1):
                r = line.RayWithLineSegment(i[j % len(i)], i[(j + 1) % len(i)])
                if r[1]:
                    if r[0] == 0:lastSide *= -1
                else:
                    if r[0] == 0:
                        points += 1
                        lastSide *= -1
                    elif r[0] == 1:
                        if lastSide == -1:points += 1
                        lastSide = 1
                    elif r[0] == -1:
                        if lastSide == 1:points += 1
                        lastSide = -1
        if points % 2 == 0:
            return 1
        else:
            return -1
    def splitToTriangles(self):
        print 'start splitting'
        outer = self.children[0]
        inners = self.children[1:]
        if self.edge_num() <= 3:
            return [outer]if self.edge_num() == 3 else []
        else:
            candidate = set(outer)
            for ring in inners:
                candidate.update(set(ring))
            eliminate = set()
            for one in candidate:
                if not self.on_line_left(one, (outer[0], outer[1])):
                    eliminate.add(one)
            candidate -= eliminate
            eliminate.clear()
            edges = self.edges()
            edges.remove((outer[0], outer[1]))
            for one in candidate:
                if not (outer[1], one) in edges:
                    for edge in edges:
                        if outer[1] in edge or one in edge:
                            continue
                        if self.is_intersect([outer[1], one], edge):
                            eliminate.add(one)
                            break
                if one in eliminate:break
                if not (one, outer[0]) in edges:
                    for edge in edges:
                        if one in edge or outer[0] in edge:
                            continue
                        if self.is_intersect([one, outer[0]], edge):
                            eliminate.add(one)
                            break
            candidate -= eliminate
            best = candidate.pop()
            while True:
                for one in candidate:
                    if self.p_in_tri_circle(one, best, outer[0], outer[1]):
                        best = one
                        candidate.remove(one)
                        break
                else:
                    break
            result = [[outer[0], outer[1], best]]
            for idx, v in enumerate(outer):
                if v == best:
                    outer1 = MultiPolygon(outer[idx:] + [outer[0]])
                    outer2 = MultiPolygon(outer[1:idx + 1])
                    if len(outer1[0]) < 3:
                        for i in inners:
                            outer2.append(i)
                        result += outer2.splitToTriangles()
                    elif len(outer2[0]) < 3:
                        for i in inners:
                            outer1.append(i)
                        result += outer1.splitToTriangles()
                    else:
                        for i in inners:
                            if outer1.IntersectionWithpoint(i[0]) == 1:
                                outer1.append(i)
                            else:
                                outer2.append(i)
                        result += outer1.splitToTriangles()
                        result += outer2.splitToTriangles()
                    break
            else:
                for i in inners:
                    for idx, v in enumerate(i):
                        if v == best:
                            outer1 = MultiPolygon(outer[1:] + [outer[0]] + i[idx:] + i[:idx + 1])
                            inners.remove(i)
                            for k in inners:
                                outer1.append(k)
                            for one in outer1.children:
                                print one
                            result += outer1.splitToTriangles()
                            break
            return result


    
            
        
        
                
