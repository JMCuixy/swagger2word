<%-- text/html：正常的html显示  application/msword：html页面直接转word--%>
<%--<%@ page contentType="application/msword" pageEncoding="UTF-8" language="java" %>--%>
<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <title>tool</title>
        <style type="text/css">
            .bg {
            background-color: rgb(84, 127, 177);
            }

            tr {
            height: 20px;
            font-size: 12px;
            }

            .specialHeight {
            height: 40px;
            }
        </style>
        <style>
            pre {outline: 1px solid #ccc; padding: 2px; margin: 2px; font-size: 12px}
            .string { color: green; }
            .number { color: darkorange; }
            .boolean { color: blue; }
            .null { color: magenta; }
            .key { color: red; }
        </style>

    </head>
    <body>
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>

        <div style="width:800px; margin: 0 auto">
            <c:forEach items="${table}" var="t">
                <h4>${t.title}</h4> <%--这个是类的说明--%>
                <h5>${t.tag}</h5>   <%--这个是每个请求的说明，方便生成文档后进行整理--%>
                <table border="1" cellspacing="0" cellpadding="0" width="100%">
                    <tr class="bg">
                        <td colspan="5"><c:out value="${t.tag}"/></td>
                    </tr>
                    <tr>
                        <td>接口描述</td>
                        <td colspan="4">${t.description}</td>
                    </tr>
                    <tr>
                        <td>URL</td>
                        <td colspan="4">${t.url}</td>
                    </tr>
                    <tr>
                        <td>请求方式</td>
                        <td colspan="4">${t.requestType}</td>
                    </tr>
                    <tr>
                        <td>请求类型</td>
                        <td colspan="4">${t.requestForm}</td>
                    </tr>
                    <tr>
                        <td>返回类型</td>
                        <td colspan="4">${t.responseForm}</td>
                    </tr>

                    <tr class="bg" align="center">
                        <td>参数名</td>
                        <td>数据类型</td>
                        <td>参数类型</td>
                        <td>是否必填</td>
                        <td>说明</td>
                    </tr>
                    <c:forEach items="${t.requestList}" var="req">
                        <tr align="center">
                            <td>${req.name}</td>
                            <td>${req.type}</td>
                            <td>${req.paramType}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${req.require == true}">Y</c:when>
                                    <c:otherwise>N</c:otherwise>
                                </c:choose>
                            </td>
                            <td>${req.remark}</td>
                        </tr>
                    </c:forEach>
                    <tr class="bg" align="center">
                        <td>状态码</td>
                        <td>描述</td>
                        <td colspan="3">说明</td>
                    </tr>

                    <c:forEach items="${t.responseList}" var="res">
                        <tr align="center">
                            <td>${res.name}</td>
                            <td>${res.description}</td>
                            <td colspan="3">${res.remark}</td>
                        </tr>
                    </c:forEach>

                    <tr class="bg">
                        <td colspan="5">示例</td>
                    </tr>
                    <tr class="specialHeight">
                        <td class="bg">请求参数</td>
                        <td colspan="4">${t.requestParam}</td>
                    </tr>
                    <tr class="specialHeight">
                        <td class="bg">返回值</td>
                        <td colspan="4"><pre>${t.responseParam}</pre></td>
                    </tr>
                </table>
                <br>
            </c:forEach>
        </div>

        <div style="width: 800px; margin: 0 auto">
            <h4>实体类结构说明</h4>
            <table border="1" cellspacing="0" cellpadding="0" width="100%">
                <tr class="specialHeight">
                    <td class="bg">类名</td>
                    <td class="bg">结构</td>
                </tr>
                <c:forEach items="${map}" var="m">
                    <c:set var="key" value="${m.key}" ></c:set>
                    <c:set var="value" value="${m.value}"></c:set>
                    <tr class="specialHeight">
                        <td class="bg">${key}</td>
                        <td><pre>${value}</pre></td>
                    </tr>
                </c:forEach>
            </table>
        </div>

    <!-- script type="text/javascript">
        $(document).ready(function () {
            var map = ${map};
            for(var key in map) {
                var str = JSON.stringify(JSON.parse(map[key]), null, 2);
                var cl = "." + key;
                $(cl).html(str);
            }
        })
    </script>
    <script type="text/javascript">
        function syntaxHighlight(json) {
            if (typeof json != 'string') {
                json = JSON.stringify(json, undefined, 2);
            }
            json = json.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>');
            return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
                var cls = 'number';
                if (/^"/.test(match)) {
                    if (/:$/.test(match)) {
                        cls = 'key';
                    } else {
                        cls = 'string';
                    }
                } else if (/true|false/.test(match)) {
                    cls = 'boolean';
                } else if (/null/.test(match)) {
                    cls = 'null';
                }
                return '<span class="' + cls + '">' + match + '</span>';
            });
        }
    </script -->
    </body>
</html>

